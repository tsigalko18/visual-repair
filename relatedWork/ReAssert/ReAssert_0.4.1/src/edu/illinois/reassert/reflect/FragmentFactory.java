package edu.illinois.reassert.reflect;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtTry;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtSimpleType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.support.visitor.ElementReplacer;

public class FragmentFactory {

	private Factory factory;

	public FragmentFactory(Factory factory) {
		this.factory = factory;
	}
	
	public Factory getFactory() {
		return factory;
	}
	
	/**
	 * Overwrite the given element with its new fragment.
	 */
	public SourceCodeFragment modify(CtElement modified) {
		return replace(modified, modified);
	}
	
	/**
	 * Remove the given element from the internal model and
	 * return the corresponding fragment.
	 */
	public SourceCodeFragment remove(CtElement toRemove) {
		return replace(toRemove, null);
	}
	
	/**
	 * Replace oldStatement with newStatement in the internal model and
	 * return the corresponding fragment.  If newElem is null, then 
	 * oldElem is removed from the model. 
	 */
	public SourceCodeFragment replace(
			CtElement oldElem, CtElement newElem) {
		if (oldElem.getParent() == null) {
			throw new IllegalArgumentException("Parent is not set");
		}
		SourcePosition oldPosition = oldElem.getPosition();
		if (oldPosition == null) {
			throw new IllegalArgumentException("Position is not set");
		}		
		replaceReflectively(oldElem, newElem);
		if (newElem != null && oldElem != newElem) {
			setParents(oldElem.getParent(), newElem);
			newElem.setPosition(oldPosition);
		}
		
		int[] fragPosition = calculateFragmentPosition(oldElem, newElem);
		int start = fragPosition[0];
		int end = fragPosition[1];
		
		SourceCodeFragment frag = new SourceCodeFragment();
		frag.code = newElem == null ? "" : print(newElem);
		frag.position = start;
		frag.replacementLength = end - start;
		
		CompilationUnit cu = oldPosition.getCompilationUnit();
		addFragment(cu, frag);
		shiftLineNumbers(cu, frag, oldPosition);
		
		return frag;
	}

	/**
	 * Add the given fragment to the given compilation unit, removing old fragments
	 * if necessary.
	 */
	protected void addFragment(CompilationUnit cu, SourceCodeFragment frag) {
		List<SourceCodeFragment> frags = cu.getSourceCodeFraments();
		if (frags != null) {
			// Spoon's addSourceCodeFragment does not replace overlapping
			// fragments.  Since ReAssert does that a lot, we need to do it here.
			ListIterator<SourceCodeFragment> fragIter = frags.listIterator();
			while (fragIter.hasNext()) {
				SourceCodeFragment existing = fragIter.next();
				if (areOverlapping(existing, frag)) {
					// replace overlapping fragment
					fragIter.remove();
					fragIter.add(frag);
					return;
				}
			}
		}
		cu.addSourceCodeFragment(frag);
	}

	protected boolean areOverlapping(
			SourceCodeFragment oldFrag,
			SourceCodeFragment newFrag) {
		int newStart = newFrag.position;
		int newEnd = newStart + newFrag.replacementLength;
		int oldStart = oldFrag.position;
		int oldEnd = oldStart + oldFrag.replacementLength;
		return newStart <= oldStart	&& oldStart <= newEnd
			|| newStart <= oldEnd && oldEnd <= newEnd;
	}

	/**
	 * Pretty-print the given element
	 */
	protected String print(final CtElement elem) {
		CompilationUnit cu = elem.getPosition().getCompilationUnit();
		
		CtTypeReference<?> junit3 = getFactory().Assert3().getAssertTypeReference();
		CtTypeReference<?> junit4 = getFactory().Assert4().getAssertTypeReference();

		ImportScanner importer = new ImportScanner();
		importer.ignore(elem);
		Set<CtTypeReference<?>> imports = importer.makeImports(cu);
		imports.add(junit4);
		imports.add(junit3);		

		StaticImportScanner siImporter = new StaticImportScanner();
		siImporter.importStaticFrom(junit3);
		siImporter.importStaticFrom(junit4);
		Set<CtExecutableReference<?>> staticImports = siImporter.makeStaticImports(cu);
		
		ReAssertPrettyPrinter pp = new ReAssertPrettyPrinter(
				getFactory().getEnvironment());
		pp.setTabCount(getNestingDepth(elem));
		pp.addImports(imports);
		pp.addStaticImports(staticImports);
		
		return pp.print(elem);
	}

	protected int getNestingDepth(CtElement elem) {
		if (elem == null || elem instanceof CtPackage) {
			// package has nesting depth -1 (top-level class has 0), 
			// subtract 1 again because we ignore the target type
			return -2; 
		}
		return 1 + getNestingDepth(elem.getParent());
	}	
	
	/**
	 * Calculate the character offsets for the beginning and end of 
	 * the given element.  Necessary because the character offsets stored 
	 * in the element positions do not always correspond to the full text 
	 * of the element.
	 * <br />
	 * This assumes "sane" whitespace and that the types are imported and 
	 * not written fully-qualified.
	 * @param newElem 
	 */
	protected int[] calculateFragmentPosition(CtElement oldElem, CtElement newElem) {
		SourcePosition position = oldElem.getPosition();
		int start = position.getSourceStart();
		int end = position.getSourceEnd();
		
		if (oldElem instanceof CtVariable) {
			CtVariable<?> var = (CtVariable<?>) oldElem;
			for (ModifierKind modifier : var.getModifiers()) {
				start -= modifier.toString().length() + 1;
			}
			CtTypeReference<?> varType = var.getType();
			while (varType instanceof CtArrayTypeReference) {
				varType = ((CtArrayTypeReference<?>) varType).getComponentType();
				start -= 2; // square brackets
			}
			start -= varType.getSimpleName().length() + 1;
			List<CtTypeReference<?>> generics = varType.getActualTypeArguments();
			if (generics.size() > 0) {
				start--; // angle bracket
				for (CtTypeReference<?> generic : generics) {
					start -= generic.getSimpleName().length();
					start -= 2; // comma-space (or comma-closing bracket)
				}
				start++; // remove last comma
			}
			CtExpression<?> init = var.getDefaultExpression();
			if (init != null && init.getPosition() != null) {
				end = init.getPosition().getSourceEnd();
			}
			end++;
		}
		
		if (oldElem instanceof CtExpression) {
			end++;
		}	
		
		if (oldElem instanceof CtBlock) {
			if (oldElem.getParent() instanceof CtExecutable) {
				start--;
				end++;
			}
			end++; // closing brace
		}
		else if (oldElem instanceof CtTry) {
			end++; // closing brace			
		}
		else if (newElem == null 
					|| newElem instanceof CtBlock
					|| newElem instanceof CtTry) {
			end++; // old semicolon
		}
		
		return new int[] { start, end }; 
	}

	/**
	 * Adapted from {@link ElementReplacer#replaceIn}.
	 * If newElem == null, then the element is removed completely.
	 */
	@SuppressWarnings("unchecked")
	protected void replaceReflectively(
			CtElement oldElem,
			CtElement newElem) {
		assert oldElem.getParent() != null;
		if (oldElem == newElem) {
			return;
		}
		CtElement parent = oldElem.getParent();
		Field[] fields = parent.getClass().getDeclaredFields();
		try {
			for (Field field : fields) {
				field.setAccessible(true);
				Object fieldValue = field.get(parent);
				
				if (fieldValue instanceof List) {
					ListIterator iter = ((List) fieldValue).listIterator();
					while (iter.hasNext()) {
						if (oldElem == iter.next()) {
							iter.remove();
							if (newElem != null) {
								iter.add(newElem);
							}
							return;
						}
					}
				}
				else if (oldElem == fieldValue) {
					field.set(parent, newElem);
					return;
				}
			}
		}
		catch (IllegalAccessException e) {
			// fall through to exception below
		}
		throw new RuntimeException("Unable to replace element"); 
	}

	/**
	 * Set the correct parent of all children rooted at child
	 */
	protected void setParents(CtElement parent, CtElement child) {
		new ParentVisitor(parent).scan(child);
	}

	/**
	 * If this fix changes the number of lines in the compilation unit, then adjust
	 * the line number of all later elements.
	 * <br />
	 * Necessary since exceptions thrown and fixed after recompilation use the updated line
	 * numbers.
	 */
	protected void shiftLineNumbers(
			CompilationUnit cu, 
			SourceCodeFragment frag, 
			SourcePosition fixPosition) {
		final int startLine = fixPosition.getLine();
		final int endLine = fixPosition.getEndLine();
		final int originalLines = endLine - startLine + 1;
		final int offset = frag.code.split("\n").length - originalLines;
		if (offset != 0) {
			CtScanner lineScanner = new CtScanner() {
				@Override
				protected void enter(CtElement e) {
					final SourcePosition position = e.getPosition();
					if (position != null) {
						int startOffset = 0;
						int endOffset = 0;
						if (startLine < position.getLine()) {
							// fragment comes before element. shift start line
							startOffset = offset;
						}
						if (endLine <= position.getEndLine()) {
							// fragment comes before or inside element. shift end line
							endOffset = offset;
						}
						if (startOffset != 0 || endOffset != 0) {
							e.setPosition(new OffsetPosition(position, startOffset, endOffset));
						}
					}
				}
			};
			for (CtSimpleType<?> type : cu.getDeclaredTypes()) {
				type.accept(lineScanner);
			}
		}
	}
	
}
