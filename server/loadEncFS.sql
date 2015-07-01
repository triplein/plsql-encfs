call dbms_output.enable(1000000);
call dbms_java.set_output(500000);

-- Replace /path/to/ENCFS.jar with the real path!
call dbms_java.grant_permission('TRIPLE_DROPBOX', 'SYS:java.io.FilePermission', '/path/to/ENCFS.jar', 'read');
call dbms_java.grant_permission('TRIPLE_DROPBOX', 'SYS:java.security.SecurityPermission', '/path/to/ENCFS.jar', 'read');
call dbms_java.loadjava('-resolve -verbose /path/to/ENCFS.jar');