package com.example.team_16;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.team_16.database.FirebaseDB;
import com.example.team_16.models.MoodEvent;
import com.example.team_16.models.PersonalMoodHistory;
import com.example.team_16.models.UserProfile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileTest {

    @Mock
    private FirebaseDB mockFirebaseDB;

    private UserProfile userProfile;
    private static final String TEST_USER_ID = "test_user_id";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_FULL_NAME = "Test User";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PROFILE_IMAGE_URL = "http://example.com/image.jpg";

    @Before
    public void setUp() {
        userProfile = new UserProfile(mockFirebaseDB, TEST_USER_ID, TEST_USERNAME, 
                                    TEST_FULL_NAME, TEST_EMAIL, TEST_PROFILE_IMAGE_URL);
    }

    @Test
    public void testConstructor() {
        assertEquals(TEST_USER_ID, userProfile.getId());
        assertEquals(TEST_USERNAME, userProfile.getUsername());
        assertEquals(TEST_FULL_NAME, userProfile.getFullName());
        assertEquals(TEST_EMAIL, userProfile.getEmail());
        assertEquals(TEST_PROFILE_IMAGE_URL, userProfile.getProfileImageUrl());
    }

    @Test
    public void testUpdateProfile() {
        String newFullName = "New Name";
        String newEmail = "new@example.com";
        String newUsername = "newuser";
        String newProfileImageUrl = "http://example.com/new.jpg";

        // Mock successful update
        doAnswer(invocation -> {
            FirebaseDB.FirebaseCallback<Boolean> callback = invocation.getArgument(5);
            callback.onCallback(true);
            return null;
        }).when(mockFirebaseDB).updateUserProfile(
            eq(TEST_USER_ID), 
            eq(newFullName), 
            eq(newEmail), 
            eq(newUsername), 
            eq(newProfileImageUrl), 
            any()
        );

        userProfile.updateProfile(newFullName, newEmail, newUsername, newProfileImageUrl, success -> {
            assertTrue(success);
            assertEquals(newFullName, userProfile.getFullName());
            assertEquals(newEmail, userProfile.getEmail());
            assertEquals(newUsername, userProfile.getUsername());
            assertEquals(newProfileImageUrl, userProfile.getProfileImageUrl());
        });
    }

    @Test
    public void testGetFollowingList() {
        List<String> followingList = new ArrayList<>();
        followingList.add("user1");
        followingList.add("user2");

        // Mock successful retrieval
        doAnswer(invocation -> {
            FirebaseDB.FirebaseCallback<List<String>> callback = invocation.getArgument(1);
            callback.onCallback(followingList);
            return null;
        }).when(mockFirebaseDB).getFollowingList(eq(TEST_USER_ID), any());

        userProfile.getFollowingList(result -> {
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains("user1"));
            assertTrue(result.contains("user2"));
        });
    }

    @Test
    public void testGetFollowers() {
        List<String> followersList = new ArrayList<>();
        followersList.add("follower1");
        followersList.add("follower2");

        // Mock successful retrieval
        doAnswer(invocation -> {
            FirebaseDB.FirebaseCallback<List<String>> callback = invocation.getArgument(1);
            callback.onCallback(followersList);
            return null;
        }).when(mockFirebaseDB).getFollowersOfUser(eq(TEST_USER_ID), any());

        userProfile.getFollowers(result -> {
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains("follower1"));
            assertTrue(result.contains("follower2"));
        });
    }

    @Test
    public void testUnfollowUser() {
        String targetUserId = "target_user";

        // Mock successful unfollow
        doAnswer(invocation -> {
            FirebaseDB.FirebaseCallback<Boolean> callback = invocation.getArgument(2);
            callback.onCallback(true);
            return null;
        }).when(mockFirebaseDB).unfollowUser(eq(TEST_USER_ID), eq(targetUserId), any());

        userProfile.unfollowUser(targetUserId, success -> assertTrue(success));
    }

    @Test
    public void testIsFollowing() {
        String targetUserId = "target_user";
        List<String> followingList = new ArrayList<>();
        followingList.add(targetUserId);

        // Mock successful check
        doAnswer(invocation -> {
            FirebaseDB.FirebaseCallback<List<String>> callback = invocation.getArgument(1);
            callback.onCallback(followingList);
            return null;
        }).when(mockFirebaseDB).getFollowingList(eq(TEST_USER_ID), any());

        userProfile.isFollowing(targetUserId, result -> assertTrue(result));
    }

    @Test
    public void testSignOut() {
        userProfile.signOut();
        verify(mockFirebaseDB).logout();
    }

    @Test
    public void testToString() {
        String expected = "UserProfile{" +
                "id='" + TEST_USER_ID + '\'' +
                ", username='" + TEST_USERNAME + '\'' +
                ", fullName='" + TEST_FULL_NAME + '\'' +
                ", profileImageUrl='" + TEST_PROFILE_IMAGE_URL + '\'' +
                '}';
        assertEquals(expected, userProfile.toString());
    }

    @Test
    public void testGetMoodHistories() {
        PersonalMoodHistory personalHistory = userProfile.getPersonalMoodHistory();
        assertNotNull(personalHistory);
        assertEquals(TEST_USER_ID, personalHistory.getUserId());

        assertNotNull(userProfile.getFollowingMoodHistory());
    }

    @Test
    public void testRefreshMoodHistories() {
        userProfile.refreshMoodHistories();
        verify(mockFirebaseDB, times(2)).getMoodEvents(
            eq(TEST_USER_ID),
            isNull(),
            isNull(),
            isNull(),
            any()
        );
    }
} 