AddressBook <- read.csv("addressbook-distribution.csv",header= FALSE, sep=",")[,2]
Claroline   <- read.csv("claroline-distribution.csv",header= FALSE, sep=",")[,2] 
Collabtive	<- read.csv("collabtive-distribution.csv",header= FALSE, sep=",")[,2]
PPMA 		<- read.csv("ppma-distribution.csv",header= FALSE, sep=",")[,2]
All 		<- read.csv("all-distribution.csv",header= FALSE, sep=",")[,2]
names <- c("AddressBook", "Claroline", "Collabtive", "PPMA", "All")
dataList <- lapply(names, get, envir=environment())
names(dataList) <- names
par(las=1)
par(mar=c(6,2,1,0.1))
#par(cex.lab=1.5)
#par(cex.axis=1)
ylim = c(0, 10)
boxplot(dataList, col=c("grey","white","grey","white","grey"), outline=FALSE)