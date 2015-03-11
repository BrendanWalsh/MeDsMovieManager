@echo Generate Keys...

del MovieManager.cer
del MovieManager.jar

if  exist  keys  goto  signjar
keytool -genkey -alias signFiles -keystore keys -keypass yourpassword -storepass yourpassword2


:signjar
@echo Signing the JAR File
jarsigner -keystore keys -storepass yourpassword2 -keypass yourpassword -signedjar MovieManager.jar MovieManager\MovieManager.jar signFiles

@echo Export the Public Key Certificate
keytool -export -keystore keys -storepass yourpassword2 -alias signFiles -file MovieManager.cer

del MovieManager\MovieManager.cer
del MovieManager\MovieManager.jar

move MovieManager.jar MovieManager\
move MovieManager.cer MovieManager\