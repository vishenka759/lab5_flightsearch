Place the pre-populated SQLite database file for the lab in this folder.

Expected file name: flight_search.db

You can download it from the lab repository and copy it here:
https://github.com/ISTU-PO-ANDROID/LabsData/blob/65c3c73c33fe4708c90fb0d4a4f5bd651820fb57/flight_search.db

The Room database is configured with:
- databaseName = "flight_search.db"
- createFromAsset("flight_search.db")

So the final path should be:
app/src/main/assets/flight_search.db

