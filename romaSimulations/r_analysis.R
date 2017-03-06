staticData <- read.csv("C:\\Users\\Francisco\\git2\\roma\\romaSimulations\\data\\trip_1000.csv", stringsAsFactors=F)
dynamicData <- read.csv("C:\\Users\\Francisco\\git2\\roma\\romaSimulations\\data\\trip2_1000.csv", stringsAsFactors=F)

summary(staticData)
summary(dynamicData)

dfStatic <- data.frame(staticData)
dfDynamic <- data.frame(dynamicData)

summary(dfStatic)
summary(dfDynamic)

head(dfDynamic)

dfStatic$duration <- as.numeric(dfStatic$duration)
dfStatic$timeLoss <- as.numeric(dfStatic$timeLoss)

dfDynamic$duration <- as.numeric(dfDynamic$duration)
dfDynamic$timeLoss <- as.numeric(dfDynamic$timeLoss)

summary(hlStatic)
summary(hlDynamic)

hlStatic <- dfStatic[dfStatic$departLane=="E5_0", ]
hlDynamic <- dfDynamic[dfDynamic$departLane=="E5_0", ]