package app.mediatracker.seed;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import app.mediatracker.feature.user.model.User;

@Component
public class DemoUserData {

    public List<User> createDemoUsers() {
        List<User> userTestData = new ArrayList<>();

        User alice = new User();
        alice.setUsername("alice");
        alice.setPasswordHash("hash1");
        alice.setPublicList(true);
        alice.setProfilePictureUrl("https://randomuser.me/api/portraits/women/1.jpg");
        userTestData.add(alice);

        User bob = new User();
        bob.setUsername("bob");
        bob.setPasswordHash("hash2");
        bob.setPublicList(true);
        bob.setProfilePictureUrl("https://randomuser.me/api/portraits/men/1.jpg");
        userTestData.add(bob);

        User charlie = new User();
        charlie.setUsername("charlie");
        charlie.setPasswordHash("hash3");
        charlie.setPublicList(true);
        charlie.setProfilePictureUrl("https://randomuser.me/api/portraits/men/2.jpg");
        userTestData.add(charlie);

        User diana = new User();
        diana.setUsername("diana");
        diana.setPasswordHash("hash4");
        diana.setPublicList(true);
        diana.setProfilePictureUrl("https://randomuser.me/api/portraits/women/2.jpg");
        userTestData.add(diana);

        User edward = new User();
        edward.setUsername("edward");
        edward.setPasswordHash("hash5");
        edward.setPublicList(true);
        edward.setProfilePictureUrl("https://randomuser.me/api/portraits/men/3.jpg");
        userTestData.add(edward);

        User fiona = new User();
        fiona.setUsername("fiona");
        fiona.setPasswordHash("hash6");
        fiona.setPublicList(true);
        fiona.setProfilePictureUrl("https://randomuser.me/api/portraits/women/3.jpg");
        userTestData.add(fiona);

        User george = new User();
        george.setUsername("george");
        george.setPasswordHash("hash7");
        george.setPublicList(true);
        george.setProfilePictureUrl("https://randomuser.me/api/portraits/men/4.jpg");
        userTestData.add(george);

        User hannah = new User();
        hannah.setUsername("hannah");
        hannah.setPasswordHash("hash8");
        hannah.setPublicList(true);
        hannah.setProfilePictureUrl("https://randomuser.me/api/portraits/women/4.jpg");
        userTestData.add(hannah);

        User ian = new User();
        ian.setUsername("ian");
        ian.setPasswordHash("hash9");
        ian.setPublicList(true);
        ian.setProfilePictureUrl("https://randomuser.me/api/portraits/men/5.jpg");
        userTestData.add(ian);

        User julia = new User();
        julia.setUsername("julia");
        julia.setPasswordHash("hash10");
        julia.setPublicList(true);
        julia.setProfilePictureUrl("https://randomuser.me/api/portraits/women/5.jpg");
        userTestData.add(julia);

        User kevin = new User();
        kevin.setUsername("kevin");
        kevin.setPasswordHash("hash11");
        kevin.setPublicList(true);
        kevin.setProfilePictureUrl("https://randomuser.me/api/portraits/men/6.jpg");
        userTestData.add(kevin);

        User laura = new User();
        laura.setUsername("laura");
        laura.setPasswordHash("hash12");
        laura.setPublicList(true);
        laura.setProfilePictureUrl("https://randomuser.me/api/portraits/women/6.jpg");
        userTestData.add(laura);

        User mike = new User();
        mike.setUsername("mike");
        mike.setPasswordHash("hash13");
        mike.setPublicList(true);
        mike.setProfilePictureUrl("https://randomuser.me/api/portraits/men/7.jpg");
        userTestData.add(mike);

        User nina = new User();
        nina.setUsername("nina");
        nina.setPasswordHash("hash14");
        nina.setPublicList(true);
        nina.setProfilePictureUrl("https://randomuser.me/api/portraits/women/7.jpg");
        userTestData.add(nina);

        User oliver = new User();
        oliver.setUsername("oliver");
        oliver.setPasswordHash("hash15");
        oliver.setPublicList(true);
        oliver.setProfilePictureUrl("https://randomuser.me/api/portraits/men/8.jpg");
        userTestData.add(oliver);

        User paula = new User();
        paula.setUsername("paula");
        paula.setPasswordHash("hash16");
        paula.setPublicList(true);
        paula.setProfilePictureUrl("https://randomuser.me/api/portraits/women/8.jpg");
        userTestData.add(paula);

        User quentin = new User();
        quentin.setUsername("quentin");
        quentin.setPasswordHash("hash17");
        quentin.setPublicList(true);
        quentin.setProfilePictureUrl("https://randomuser.me/api/portraits/men/9.jpg");
        userTestData.add(quentin);

        User rachel = new User();
        rachel.setUsername("rachel");
        rachel.setPasswordHash("hash18");
        rachel.setPublicList(true);
        rachel.setProfilePictureUrl("https://randomuser.me/api/portraits/women/9.jpg");
        userTestData.add(rachel);

        User steve = new User();
        steve.setUsername("steve");
        steve.setPasswordHash("hash19");
        steve.setPublicList(true);
        steve.setProfilePictureUrl("https://randomuser.me/api/portraits/men/10.jpg");
        userTestData.add(steve);

        User tina = new User();
        tina.setUsername("tina");
        tina.setPasswordHash("hash20");
        tina.setPublicList(true);
        tina.setProfilePictureUrl("https://randomuser.me/api/portraits/women/10.jpg");
        userTestData.add(tina);

        return userTestData;
    }
}
