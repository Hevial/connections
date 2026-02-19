import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import models.User;
import server.db.DBConfig;
import server.db.DBManager;
import server.db.DBStatus;

class DBManagerTest {

    private DBManager dbManager;
    private int testsRun = 0;
    private int testsPassed = 0;
    private String usersPath;
    private String usersBackup;

    void setUp() {
        dbManager = DBManager.getInstance();
    }

    void backupUsersFile() {
        try {
            usersPath = DBConfig.loadConfig().getUsersPath();
            Path path = Paths.get(usersPath);
            usersBackup = Files.exists(path) ? Files.readString(path) : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to backup users file", e);
        }
    }

    void restoreUsersFile() {
        if (usersPath == null) {
            return;
        }
        try {
            Path path = Paths.get(usersPath);
            if (usersBackup == null) {
                Files.deleteIfExists(path);
            } else {
                Files.writeString(path, usersBackup, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            System.out.println("Warning: Failed to restore users file: " + e.getMessage());
        }
    }

    String uniqueUsername(String prefix) {
        return prefix + "_" + System.nanoTime() + "_" + (int) (Math.random() * 10000);
    }

    void assertTrue(boolean condition, String testName) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println(testName + " passed");
        } else {
            System.out.println(testName + " failed");
        }
    }

    void testAddNewUserSuccess() {
        setUp();
        String username = uniqueUsername("newuser");
        User newUser = new User(username, "pass" + System.nanoTime());
        boolean result = dbManager.addNewUser(newUser);
        User retrievedUser = dbManager.getUserByUsername(username);
        assertTrue(result && retrievedUser == newUser, "testAddNewUserSuccess");
    }

    void testAddNewUserAlreadyExists() {
        setUp();
        String username = uniqueUsername("existing");
        User firstUser = new User(username, "password1");
        User secondUser = new User(username, "password2");
        boolean firstResult = dbManager.addNewUser(firstUser);
        boolean secondResult = dbManager.addNewUser(secondUser);
        User retrievedUser = dbManager.getUserByUsername(username);
        assertTrue(firstResult && !secondResult
                && retrievedUser != null
                && "password1".equals(retrievedUser.getPassword()),
                "testAddNewUserAlreadyExists");
    }

    void testAddNewUserWithNullUsername() {
        setUp();
        try {
            User nullUser = new User(null, "password");
            dbManager.addNewUser(nullUser);
            assertTrue(false, "testAddNewUserWithNullUsername");
        } catch (NullPointerException e) {
            assertTrue(true, "testAddNewUserWithNullUsername");
        }
    }

    void testGetUserByUsername() {
        setUp();
        String username = uniqueUsername("lookup");
        User testUser = new User(username, "password");
        dbManager.addNewUser(testUser);
        User retrievedUser = dbManager.getUserByUsername(username);
        assertTrue(retrievedUser != null && retrievedUser == testUser, "testGetUserByUsername");
    }

    void testGetUserByUsernameNotFound() {
        setUp();
        String username = uniqueUsername("missing");
        while (dbManager.getUserByUsername(username) != null) {
            username = uniqueUsername("missing");
        }
        User retrievedUser = dbManager.getUserByUsername(username);
        assertTrue(retrievedUser == null, "testGetUserByUsernameNotFound");
    }

    void testLoginUserSuccess() {
        setUp();
        String username = uniqueUsername("login");
        dbManager.addNewUser(new User(username, "password"));
        DBStatus result = dbManager.loginUser(username, "missing_password");
        assertTrue(result == DBStatus.SUCCESS, "testLoginUserSuccess");
    }

    void testLoginUserNotFound() {
        setUp();
        String username = uniqueUsername("missing_login");
        while (dbManager.getUserByUsername(username) != null) {
            username = uniqueUsername("missing_login");
        }
        DBStatus result = dbManager.loginUser(username, "missing_password");
        assertTrue(result != DBStatus.SUCCESS, "testLoginUserNotFound");
    }

    void testLogoutUserSuccess() {
        setUp();
        String username = uniqueUsername("logout");
        dbManager.addNewUser(new User(username, "password"));
        dbManager.loginUser(username, "missing_password");
        boolean result = dbManager.logoutUser(username);
        assertTrue(result, "testLogoutUserSuccess");
    }

    void testLogoutUserNotLoggedIn() {
        setUp();
        String username = uniqueUsername("not_logged_in");
        boolean result = dbManager.logoutUser(username);
        assertTrue(!result, "testLogoutUserNotLoggedIn");
    }

    public static void main(String[] args) {
        DBManagerTest tests = new DBManagerTest();
        tests.backupUsersFile();
        try {
            tests.testAddNewUserSuccess();
            tests.testAddNewUserAlreadyExists();
            tests.testAddNewUserWithNullUsername();
            tests.testGetUserByUsername();
            tests.testGetUserByUsernameNotFound();
            tests.testLoginUserSuccess();
            tests.testLoginUserNotFound();
            tests.testLogoutUserSuccess();
            tests.testLogoutUserNotLoggedIn();
        } finally {
            tests.restoreUsersFile();
        }
        System.out.println("\nTests passed: " + tests.testsPassed + "/" + tests.testsRun);
    }
}