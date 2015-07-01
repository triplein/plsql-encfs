--------------------------------------------------------
--  Datei erstellt -Donnerstag-September-04-2014   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Package DOCENCFS
--------------------------------------------------------

  CREATE OR REPLACE PACKAGE "DOCENCFS" as 

  function fDecrypt(i_lbEncryptedData in     blob
                      ,i_lbConfig        in     blob
                      ,i_vcPassword      in     varchar2
                      )return blob;


  function fEncrypt(i_lbUnencryptedData in     blob
                      ,i_lbConfig          in     blob
                      ,i_vcPassword        in     varchar2
                      )return blob;
                      
  function fDecryptFilename(i_vcEncryptedFilename  in      varchar2
                              ,i_lbConfig             in      blob
                              ,i_vcPassword           in      varchar2
                              )return varchar2;
                        
  function fEncryptFilename(i_vcUnencryptedFilename in     varchar2
                              ,i_lbConfig              in     blob
                              ,i_vcPassword            in     varchar2
                              )return varchar2;
                              
  function fGenerateBoxCryptorConfig(i_vcPassword in     varchar2) return blob;
end docEncFS;

/
--------------------------------------------------------
--  DDL for Package Body DOCENCFS
--------------------------------------------------------

  CREATE OR REPLACE PACKAGE BODY "DOCENCFS" as 

  function fDecrypt(i_lbEncryptedData in     blob
                   ,i_lbConfig        in     blob
                   ,i_vcPassword      in     varchar2
                   )return blob as
      language java name 'at.triplein.singleFileEncFS.EncFSSingleFile.decrypt(oracle.sql.BLOB, oracle.sql.BLOB, java.lang.String) return oracle.sql.BLOB';

  function fEncrypt(i_lbUnencryptedData in     blob
                   ,i_lbConfig          in     blob
                   ,i_vcPassword        in     varchar2
                   )return blob as
      language java name 'at.triplein.singleFileEncFS.EncFSSingleFile.encrypt(oracle.sql.BLOB, oracle.sql.BLOB, java.lang.String) return oracle.sql.BLOB';
                      
  function fDecryptFilename(i_vcEncryptedFilename  in      varchar2
                           ,i_lbConfig             in      blob
                           ,i_vcPassword           in      varchar2
                           )return varchar2 as
      language java name 'at.triplein.singleFileEncFS.EncFSSingleFile.decryptFilename(java.lang.String, oracle.sql.BLOB, java.lang.String) return java.lang.String';
                        
  function fEncryptFilename(i_vcUnencryptedFilename in     varchar2
                           ,i_lbConfig              in     blob
                           ,i_vcPassword            in     varchar2
                           )return varchar2 as
      language java name 'at.triplein.singleFileEncFS.EncFSSingleFile.encryptFilename(java.lang.String, oracle.sql.BLOB, java.lang.String) return java.lang.String';
    
  function fGenerateBoxCryptorConfig(i_vcPassword in     varchar2) return blob as
      language java name 'at.triplein.singleFileEncFS.EncFSSingleFile.generateBoxcryptorConfigFileAsBlob(java.lang.String) return oracle.sql.BLOB';
    
end docEncFS;

/
