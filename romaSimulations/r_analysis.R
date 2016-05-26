staticData <- read.csv("C:\\Users\\Francisco\\git\\roma\\romaSimulations\\data\\tripStatic1500.csv", stringsAsFactors=F)
dynamicData <- read.csv("C:\\Users\\Francisco\\git\\roma\\romaSimulations\\data\\tripDynamic1501.csv", stringsAsFactors=F)

summary(staticData)
summary(dynamicData)

dfStatic <- data.frame(staticData)
hlStatic <- dfStatic[dfStatic$departLane=="E5_0", ]

dfDynamic <- data.frame(dynamicData)
hlDynamic <- dfDynamic[dfDynamic$departLane=="E5_0", ]

summary(hlStatic)
summary(hlDynamic)
