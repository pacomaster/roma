staticData <- read.csv("C:\\Users\\Francisco\\git\\roma\\romaSimulations\\data\\trip.csv", stringsAsFactors=F)
dynamicData <- read.csv("C:\\Users\\Francisco\\git\\roma\\romaSimulations\\data\\trip2.csv", stringsAsFactors=F)

staticData <- read.csv("C:\\Users\\famezcua\\PROJECTS\\Master ITESO\\sumo-0.25.0\\bin\\data\\trip.csv", stringsAsFactors=F)

head(staticData)

summary(staticData)
summary(dynamicData)

dfStatic <- data.frame(staticData)
dfStatic$duration <- as.numeric(dfStatic$duration)
dfStatic$timeLoss <- as.numeric(dfStatic$timeLoss)
hlStatic <- dfStatic[dfStatic$departLane=="E5_0", ]

dfDynamic <- data.frame(dynamicData)
hlDynamic <- dfDynamic[dfDynamic$departLane=="E5_0", ]

summary(hlStatic)
summary(hlDynamic)

str(staticData)
str(dfStatic)
summary(dfStatic)
