# PostgreSQL

PostgreSQL is a powerful open-source relational database management system (RDBMS), renowned for stability, scalability, and robustness. It is an ideal choice for applications that require data consistency and reliability.

## Prerequisites

* Get PostgreSQL database connection parameters from the database owner.
* Refer to IP allowlist to add IP addresses of OpenFlower to your allowlist (if needed).

## Connect to PostgreSQL data source

Follow the steps below:

1. Create a new data source in two ways. Note that this permission is restricted to workspace admins and developers.
   * Navigate to the **Data Sources** tab on [OpenFlower Homepage](https://prod-us1.openflower.org) and click **New data source**.
   * When creating a new query in the app editor, click **+ New** > **+ New data source**.
2. Select **Database** > **PostgreSQL** as the data source type.
3. Set its name and configure general settings, including host, port, and database name. You can also set the user name and a password.
4. (Optional) Click **Test connection** to check whether the new data source is successfully connected.
5. Click **Save**, and it will be saved to your data source library.
