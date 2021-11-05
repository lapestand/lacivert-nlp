from jpype import JClass, getDefaultJVMPath, startJVM, shutdownJVM, JString, isJVMStarted, java
from typing import List
import os, traceback, sys, logging
from lacivert import properties


class JVMHelper:
    
    def __init__(self):
        self._errors: List[str] = []
        self._res: List[str] = []
        logging.info("Helper class initialized")
        

    def startJVM(self):
        logging.info("Java Virtual Machine is starting")
        startJVM(getDefaultJVMPath(), classpath=[properties.ZEMBEREK_PATH], convertStrings=False)
        
        if not self.is_jvm_ok():
            logging.error("Error while starting Java Virtual Machine. Details below.")
            for err in self._errors:
                logging.error(err)
            raise RuntimeError("Error while starting Java Virtual Machine")
        
        logging.info("Java Virtual Machine started")


    def __is_zemberek_loaded(self):
        zemberek_loaded = properties.ZEMBEREK_PATH in str(java.lang.System.getProperty("java.class.path"))

        if zemberek_loaded:
            logging.info("Zemberek Class loaded successfully")
            return True
        else:
            self._errors.append("Zemberek Class could not be loaded")
            return False


    def __is_jvm_started(self):
        jvm_started = bool(isJVMStarted())
        if jvm_started:
            return True
        else:
            self._errors.append("Java Virtual Machine could not be started")
            return False


    def is_jvm_ok(self):
        return self.__is_jvm_started() and self.__is_zemberek_loaded()

    def errors(self):
        return '\n' + '\n'.join(self._errors)

    def shutdown_jvm(self):
        shutdownJVM()



class InputHandler:
    def __init__(self):
        pass

    def get_file_content_as_str(self, file_path) -> str:
        try:
            with open(file_path, 'r', encoding=properties.ENCODING) as src:
                source_text = src.read()
        except Exception as err:
            logging.warning("Error while reading the source file!")
            traceback.print_exc()
            sys.exit(err)
        
        logging.info("Successfully readed content from file.")
        return source_text

    