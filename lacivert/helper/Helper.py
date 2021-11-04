from jpype import JClass, getDefaultJVMPath, startJVM, shutdownJVM, JString, isJVMStarted, java
from typing import List
import os
import logging
from .. import properties


class JVMHelper:
    def __init__(self):
        logging.info("Helper class initialized")
        self._errors: List[str] = []
        self._res: List[str] = []
        

    def startJVM(self):
        logging.info("Java Virtual Machine is starting")
        startJVM(getDefaultJVMPath(), classpath=[properties.ZEMBEREK_PATH], convertStrings=False)
        

    def __is_zemberek_loaded(self):
        zemberek_loaded = properties.ZEMBEREK_PATH in str(java.lang.System.getProperty("java.class.path"))

        if zemberek_loaded:
            logging.info("Zemberek Class loaded successfully")
            return True

        else:
            logging.error("Zemberek Class could not be loaded")
            self._errors.append("Zemberek Class could not be loaded")
            return False


    def __is_jvm_started(self):
        jvm_started = bool(isJVMStarted())
        if jvm_started:
            logging.info("Java Virtual Machine started") 
            return True
        else:
            logging.error("Java Virtual Machine could not be started")
            self._errors.append("Java Virtual Machine could not be started")
            return False


    def is_jvm_ok(self):
        return self.__is_jvm_started() and self.__is_zemberek_loaded()

    def errors(self):
        return '\n' + '\n'.join(self._errors)

    def shutdown_jvm(self):
        shutdownJVM()



