This project intends to provide tools to help with the GWT CellTable class, to make it as easy to use and boilerplate-free as the Editor and UiBinder code has done for many widgets.

## Cell and Column creation ##
The best way to explain would be by way of example. http://code.google.com/webtoolkit/doc/latest/DevGuideUiCellWidgets.html#celltable provides a sample demonstrating cell and column creation, simplified here to just the column creation.
    // Create a CellTable.
    CellTable<Contact> table = new CellTable<Contact>();

    // Create name column.
    TextColumn<Contact> nameColumn = new TextColumn<Contact>() {
      @Override
      public String getValue(Contact contact) {
        return contact.name;
      }
    };

    // Create address column.
    TextColumn<Contact> addressColumn = new TextColumn<Contact>() {
      @Override
      public String getValue(Contact contact) {
        return contact.address;
      }
    };

    // Add the columns.
    table.addColumn(nameColumn, "Name");
    table.addColumn(addressColumn, "Address");

A `TextColumn` object is a `Column<T,String>`, with its `Cell` instance set to a `TextCell`. The first column shown here could be also written as 
    // Create name column.
    Column<Contact, String> nameColumn = new Column<Contact, String>(new TextCell()) {
      @Override
      public String getValue(Contact contact) {
        return contact.name;
      }
    };
This allows the some simplification of some kinds of columns, but only the ones which Column subclasses are created for - it is not ideal to need to create a new `Column` subtype for each `Cell`.

Instead, as most of this seems to be rather unnecessary code (especially given what the Editor framework has brought), I suggest that the columns be declared as simply as possible
    interface ContactColumns extends Columns<Contact> {
      @Header("Name")
      TextCell name();
      @Header("Address")
      TextCell address();
    }
and can be added into the ui as follows:
    // Create a CellTable.
    CellTable<Contact> table = new CellTable<Contact>();
    // Add the columns
    ContactColumns columns = GWT.create(ContactColumns.class);
    columns.configure(table);

## Using it ##
This is still a fairly young library, and useful features are still being added, so there is no official release available yet. Sonatype's snapshot repository is being used to host builds, both for GWT versions before 2.2 and after.  
Repository:

    <repository>
        <id>sonatype-nexus-snapshots</id>
        <name>Sonatype Nexus Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
Dependency:
    <dependency>
        <groupId>com.colinalworth</groupId>
        <artifactId>celltable-tools</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>

Add a `<classifier>pre-2.2</classifier>` if using a version of GWT before 2.2. This is not designed to provide any server-side functionality, so `<scope>provided</scope>` may be desired as well.

### Building from source ###
By default, this is set up to work with 2.2, though 2.1 is supported as well. When building, use the profile '`pre-2.2`', and specify '`pre-2.2`' as a classifier in the dependency as well.
