# Change the password

As we said in [Password and encryption](DatabaseOpenCreation.md#password-and-encryption),
it is possible to change password only if database was created with one.

The only way (for now) to add password to a database created in clear, 
or to remove password form database created with one :
1. Create an other temporary database with good configuration
1. Copy paste data from one to other
1. Delete original database file
1. Rename the temporary database file to old name

As you see it is complicated and heavy.

Here we talk about change password for database created originally with one.
The operation is easy, just call the method **changePassword** of **Database**.

The operation can take time (depends on database current size). 
Moreover it is dangerous to make requests while the change play, don't read or modify database during the process.

[Back to menu](Menu.md#menu)
 