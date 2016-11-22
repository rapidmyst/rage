
myData <- read.csv(file="./ign.csv", header=TRUE, sep=",")

write.csv(levels(myData$platform), file="./platforms.csv")
write.csv(levels(myData$genre), file="./genres.csv")

n<-nrow(myData)

for(i in 1:n)
{
  myStr<-as.character(myData[i,]$genre)
  myList<-strsplit((myStr), ",")
  dat<-unlist(myList)
  myData[i,]$genre<-dat[1]
  myData[i,]$genre2<-trimws(dat[2])
}

write.csv(myData, file="./ign.csv")

