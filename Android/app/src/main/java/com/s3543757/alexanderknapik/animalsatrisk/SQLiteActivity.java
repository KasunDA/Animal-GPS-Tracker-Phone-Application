/*
PURPOSE
~~~~~~~~
This program shows the basics of Creating, Reading, Updating, and Deleting database elements (CRUD).
It also shows how to use editText elements to display part of a database.
It is best to run the code first then come back here to read about how it was done.

The database is just one table with columns id, Artist, and Music.
Database values are read into a variable "result" and then selected values into the GUI editText
boxes on screen.  The up and down buttons change which rows in the database get displayed.
Only the top GUI row is editable, the rest are for display only.  This greatly simplifies the code!


Buttons
~~~~~~~~
   Reset DB: deletes database table, re-creates it, fills it up dummy database, and displays it.
   Down, Up: changes the active row (top row of the onscreen table) and re-displays.
   Add @ End: adds a new database row at the end and sets the GUI pointing to the end.
   Delete Top Row: deletes database row corresponding to the top GUI row and re-displays.
   Save Top Row: takes the altered top row of the GUI table, writes it to database and re-displays.


Key Functions & Data
~~~~~~~~~~~~~~~~~~~~
   active_row is an integer which says which database row is to be displayed in the table GUI top
              row which is the only place where the database can be edited.

   See the onCreate to see how the database is displayed in the editText boxes.

   Look at the button click routines to see update, delete etc is done.


Other
~~~~~~
   TableLayout : in Android Studio under the palette see the Component Tree.  Move to the TableLayout
                 to see the three rows which each contain three editText boxes, and a bottom row of
                 textView to hold labels for the columns.
                 Click on Text mode to see the XML.
                 For each editText note width zero layout_width="0dp" and a weight, eg  layout_weight=".5"
                 which allows the editText items to be properly proportioned across the TableLayout.
                 If you ever want more rows then copy and paste and entire table row of XML
                 then adjust the table height.

                 The bottom row are labels, ideally should be the top but then the soft keyboard
                 then covers the top editing row. Could rearrange buttons to move the table up.


USEFUL HINTS
~~~~~~~~~~~~
 * Forming SQL statements: easiest to use the printf like String.format() rather than adding strings.
                           See butSave().
                           If you are not familiar with SQL use a tool like SQLWorkbench to create
                           the SQL.

 * SQL or helper: much SQL can be achieved by built in member functions, for example
                   db.insert().  Often it is easier to form the SQL string and submit an SQL query
                   using db.execSQL().

 * Focus: The main class has an interface that implements View.OnFocusChangeListener.
          This did not end up getting used but it is neat,  see the function for details.

 * Input Type for each editText is important, it affects the soft keyboard displayed
              and what characters are allowed.  See the text XML for eT12.

ISSUES
~~~~~~
 * End edit: it is very difficult to work out when an editText has finished being edited.
             Easiest to have a separate button the user has to press to cause action.  If this
             app the "Save Top Row" button performs this task.

 * Hide keyboard : there is not simple way to hide the soft keyboard on the Android screen.
                   This is a major omission from the API.

 * Database in thread?  For simplicity this project put database functions in the main thread which
                        has the potential to freeze GUI activity while calls to the database
                        are active.  A better solution is to put the database calls in a thread.
                        An asyncTask is suitable,  with communications being via an intent.

 * SQLite execptions?   Any SQLite call could result in an exception.  Not all have been covered
                        by try-catch blocks here.  A very reliable system would need to have
                        command, or group of commands covered by a try-catch.


USEFUL REFERENCES
~~~~~~~~~~~~~~~~~
   Table layout can be difficult,  especially getting the relative sized right.  See
     from https://www.techrepublic.com/article/pro-tip-save-hours-of-frustration-with-this-android-tablelayout-solution/

   Basic SQLite from https://www.simplifiedcoding.net/sqliteopenhelper-tutorial/

   The simple ways of adding a change of focus catcher did not work.  For a popular failed version see-
     https://stackoverflow.com/questions/10627137/how-can-i-know-when-an-edittext-loses-focus
   See instead-
     https://stackoverflow.com/questions/10627137/how-can-i-know-when-an-edittext-loses-focus

   Working out when an editText has finished being edited is a problem,  see
     http://wiresareobsolete.com/2012/02/monitoring-edittext-sessions/

   More on text input via editText
     https://101apps.co.za/articles/capturing-user-input-with-android-s-textfields.html

Pj Radcliffe 2018, GPL.

SQLiteActivity.java was based on code provided by Dr. PJ Radcliffe
Copyright (C) 2018 under the GNU General Public License.

Changes I have made to the program was to remove the original database, and use it as a template
to create my own kind of database, containing multiple tables of information regarding the
monitoring of farm animals.

*/

package com.s3543757.alexanderknapik.animalsatrisk;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnFocusChangeListener;

import static android.app.PendingIntent.getActivity;


public class SQLiteActivity extends AppCompatActivity implements View.OnFocusChangeListener { //.......
    //------ Single definition of key database names and database variables.
    private static final String DB_NAME = "MonitorAnimalsAtRisk.db";
    //First table, entity of animals.
    private static final String e_animal_table = "e_animal";
    private static final String e_animal_C1 = "animal_ID";
    private static final String e_animal_C2 = "animal_type";
    private static final String e_animal_C3 = "animal_name";
    private static final String e_animal_C4 = "animal_age";

    //Second table, the relationship between animals and their location
    private static final String r_animal_seen_in = "r_animal_seen_in";
    private static final String r_animal_seen_in_C1 = "animal_ID";
    private static final String r_animal_seen_in_C2 = "location_ID";
    private static final String r_animal_seen_in_C3 = "time_seen";

    //Third table, entity of location
    private static final String e_location_table = "e_location";
    private static final String e_location_C1 = "location_ID";
    private static final String e_location_C2 = "gps_x";
    private static final String e_location_C3 = "gps_y";

    //Fourth table, the relationship between location and their respective zones (if there are any)
    private static final String r_location = "r_animal_seen_in";
    private static final String r_location_C1 = "animal_ID";
    private static final String r_location_C2 = "location_ID";

    //Fifth table, entity of zones.
    private static final String e_zone_table = "e_zone";
    private static final String e_zone_C1 = "zone_ID";
    private static final String e_zone_C2 = "zone_name";

    //Sixth table, the relationship between zones and their type
    private static final String r_zone_is_a = "r_zone_is_a";
    private static final String r_zone_is_aC1 = "zone_ID";
    private static final String r_zone_is_aC2 = "zone_type";

    //Seventh table, entity of zone types
    private static final String e_zone_type_table = "e_zone_type";
    private static final String e_zone_type_C1 = "zone_type";
    private static final String e_zone_type_C2 = "type_name";

    //Eight table, the relationhip between animals and their recorded medical issues.
    private static final String r_animal_has_had_table = "r_animal_has_had";
    private static final String r_animal_has_had_C1 = "animal_ID";
    private static final String r_animal_has_had_C2 = "medical_issue";

    //Ninth table, entity of medical issues.
    private static final String e_medical_issue_table = "e_medical_issue";
    private static final String e_medical_issue_C1 = "medical_issue";
    private static final String e_medical_issue_C2 = "issue_description";

    //Tenth table, relationship between animals and their respective paddock.
    private static final String r_animal_belongs_to_table = "r_animal_belongs_to";
    private static final String r_animal_belongs_to_C1 = "animal_ID";
    private static final String r_animal_belongs_to_C2 = "paddock_ID";

    //Eleventh table, entity of paddocks.
    private static final String e_paddock_table = "e_paddock";
    private static final String e_paddock_C1 = "paddock_ID";
    private static final String e_paddock_C2 = "paddock_name";


    //Default string to create a table
    /*private static final String CREATE =
            "CREATE TABLE IF NOT EXISTS " + DB_TABLE + " ( " + C1 + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    //+ C2 + " TEXT," + C3 + " TEXT ) ;" ;*/

    //private static final String[] cols = { C1, C2, C3 };

    //Create table sql queries
    private static final String createTable1 =
            "CREATE TABLE IF NOT EXISTS " + e_animal_table + " ( " +
                    e_animal_C1 + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    e_animal_C2 + " TEXT," +
                    e_animal_C3 + " TEXT," +
                    e_animal_C4 + " INTEGER ) ;" ;

    private static final String createTable2 =
            "CREATE TABLE IF NOT EXISTS " + r_animal_seen_in + " ( FOREIGN KEY(" +
                    r_animal_seen_in_C1 + ") REFERENCES " + e_animal_table + "(" + e_animal_C1 + ")," +
                    r_animal_seen_in_C2 + ") REFERENCES " + e_location_table + "(" + e_location_C1 + ")," +
                    r_animal_seen_in_C3 + " INTEGER ) ;" ;

    private static final String createTable3 =
            "CREATE TABLE IF NOT EXISTS " + e_location_table + " ( " +
                    e_location_C1 + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    e_location_C2 + " REAL," +
                    e_location_C3 + " REAL ) ;" ;


    String sql_tmp ;
    private SQLiteDatabase db ;

    private int activeRow ;
    private int nRows ;
    Cursor result ;
    ContentValues cv;

    /*
    //--- All the GUI elements references
    EditText et_11, et_12, et_13 ;
    EditText et_21, et_22, et_23 ;
    EditText et_31, et_32, et_33 ;

    //--- no button references needed as only respond to a click no changing of properties.

    //---
    int active_row = 3 ;  // database row to display on top GUI row, numbered from 1.
    */

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) { //......................................
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_zz);

        //--- get references to the GUI items that need to be accessed.
        //et_11 = (EditText) findViewById(R.id.eT11);
        //et_12 = (EditText) findViewById(R.id.eT12);
        //et_13 = (EditText) findViewById(R.id.eT13);
        //et_21 = (EditText) findViewById(R.id.eT21);
        //et_22 = (EditText) findViewById(R.id.eT22);
        //et_23 = (EditText) findViewById(R.id.eT23);
        //et_31 = (EditText) findViewById(R.id.eT31);
        //et_32 = (EditText) findViewById(R.id.eT32);
        //et_33 = (EditText) findViewById(R.id.eT33);

        //et_12.setOnFocusChangeListener(this);        // enable listening to change of focus.
        //et_13.setOnFocusChangeListener(this);

        //--- creates/opens database in private area for application.
        db = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        try // try to open the table music. If it does not exist then create it and fill it.
        {  result = db.query(DB_TABLE, null,
                null, null, null, null, null);
        }
        catch (SQLiteException e) { // DB_TABLE does not exist so create it, and fill it.
            db.execSQL(CREATE) ;
            fillDatabase( null);
        }

        //--- Display database in GUI.
        activeRow = 2 ;
        displayDB() ;
    }
    */

    /*
    //------ Display database on table.
    void displayDB() { // .......................................................................
        //--- clear all editText individual column displays.
        et_11.setText("") ; et_12.setText("") ; et_13.setText("") ;
        et_21.setText("") ; et_22.setText("") ; et_23.setText("") ;
        et_31.setText("") ; et_32.setText("") ; et_33.setText("") ;

        //--- get database content into result, find where active_row is.
        result = db.query(DB_TABLE, cols, null, null, null, null, null);
        if ( result == null) return ;
        nRows = result.getCount(); ;
        int tmp = active_row ;
        boolean move_OK = true ;
        while ( tmp>0) { move_OK = result.moveToNext() ; tmp-- ; }
        if ( ! move_OK) return ;

        //--- fill up editText boxes from database starting from active_row.

        et_11.setText(result.getString(0));
        et_12.setText(result.getString(1));
        et_13.setText(result.getString(2));

        if (result.moveToNext() ) {
            et_21.setText(result.getString(0));
            et_22.setText(result.getString(1));
            et_23.setText(result.getString(2));
        }
        if (result.moveToNext() ) {
            et_31.setText(result.getString(0));
            et_32.setText(result.getString(1));
            et_33.setText(result.getString(2));
        }
    }

//====== Respond to buttons .....................................................................

//------ Create database complete with dummy data.

    public void fillDatabase(View view) {
        //--- clear table and reset the autoinc counter.
        db.execSQL("DELETE FROM " + DB_TABLE + " ;") ;
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DB_TABLE + "'");

        //--- using ContentValues add data.
        cv = new ContentValues();
        cv.put(C2, "Beetles"); cv.put(C3, "Hard Days Night");
        db.insert(DB_TABLE, null, cv);
        cv.put(C2, "Beetles"); cv.put(C3, "Elenor Rigby");
        db.insert(DB_TABLE, null, cv);
        cv.put(C2, "Fleetwood Mac"); cv.put(C3, "The Chain");
        db.insert(DB_TABLE, null, cv);
        cv.put(C2, "Gin Wigmore"); cv.put(C3, "Black Parade");
        db.insert(DB_TABLE, null, cv);
        cv.put(C2, "Gin Wigmore"); cv.put(C3, "New Rush");
        db.insert(DB_TABLE, null, cv);
        //--- direct SQL, faster and simpler, should have used String.format
        sql_tmp = "INSERT INTO " + DB_TABLE + " ( " + C2 + ", " + C3 + " ) " + " VALUES " ;
        db.execSQL( sql_tmp + " ('Bach', 'Toccata & Fugue in D Minor') ; ") ;
        db.execSQL( sql_tmp + " ('Telemann','Flute') ; " ) ;

        displayDB() ;
    }

    //------ Up and down buttons.
    public void butUp(View view) {
        if (active_row > 1)  --active_row ; // don't go before start, index 1.
        displayDB() ;
    }
    public void butDown(View view) {
        if (active_row < nRows) active_row++ ; // don't go beyond end.
        displayDB() ;
    }

    //------ Add, Delete, and Save buttons
    public void butAdd(View view) {
        sql_tmp = "INSERT INTO " + DB_TABLE + " ( " + C2 + ", " + C3 + " ) " + " VALUES " ;
        db.execSQL( sql_tmp + " ('dummy', 'dummy') ; ") ;
        active_row = ++nRows ; // increment nRows then assign.
        displayDB() ;
    }

    public void butDel(View view) {
        sql_tmp = et_11.getText() + " ";
        if (!et_11.getText().toString().trim().isEmpty()) { // not toString() on getText()
            sql_tmp = "DELETE FROM " + DB_TABLE + " WHERE id = " + et_11.getText() + " ;"; // et_1 row is the key.
            db.execSQL(sql_tmp);
        }
        --nRows ;
        if ( active_row > nRows)  active_row = nRows ;
        displayDB() ;
    }

    public void butSave(View view) {
        //--- user has editied top row and wants to save it.
        sql_tmp = String.format("UPDATE %s SET %s = '%s' , %s = '%s' WHERE %s = %s;",
                DB_TABLE, C2, et_12.getText(), C3, et_13.getText(), C1, et_11.getText()) ;
        db.execSQL( sql_tmp) ;
        displayDB() ;
        //--- hide keyboard.
        InputMethodManager inputMethodManager =  (InputMethodManager) this.getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow( this.getCurrentFocus().getWindowToken(), 0);
    }

*/

//====== Respond to changes in focus, from one GUI item to the next.
//         Not used but left in for interest.

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        /*
        if (!hasFocus){
            // Do not changed the focus in this routine.
            if (v == et_12) {
                sql_tmp = "12" ;
            }
            if (v == et_13) {
                sql_tmp = "13" ;
            }
            */
        }
}
