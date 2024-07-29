package com.mycompany.sinavicin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.sound.sampled.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Sinavicin extends JPanel implements KeyListener, MouseListener {

    private char direction; // Ana yön
    private char directiondik;
    private char subDirection; // Alt yön (çapraz hareketler için)
    private final int DOOR_WIDTH = 40; // Kapı genişliği
    private final int DOOR_HEIGHT = 20; // Kapı yüksekliği
    private final int DOOR_X = 1000 / 2 - 40 / 2; // Kapı X koordinatı
    private final int DOOR_GAP = 40; // Kapı boşluğu
    private final int DOOR_Y_TOP = 750 - 20; // Üst kapı Y koordinatı
    private final int DOOR_Y_BOTTOM = 0; // Alt kapı Y koordinatı
    private final int WIDTH = 1000;
    private final int HEIGHT = 750;
    private final int CELL_SIZE = 40;
    private int CAN_ZOMBİE = 3;
    private int CAN_BOSS = 10;
    private String[] zombiler;
    private HashSet<Point> zombiePositions;
    private int bulleti = 0;
    private int bulletj = 0;

    private int CAN = 3;
    private int level = 1;
    private int level2 = 1;
    private int oldurulen = 0;
    private int barWidth = 10;
    private int barHeight = 5;

    private ArrayList<Point> asker;
    private ArrayList<String> bullets;

    private Point zombie;
    private Point BOSS;
    private Point can;
    private Point weapon; // Yeni silah noktası
    private BufferedImage ımage;
    private BufferedImage ımage_asker;
    private BufferedImage ımage_zombie;
    private BufferedImage ımage_dabanca;
    private BufferedImage ımage_door;
    private BufferedImage ımage_galp;
    private BufferedImage ımage_zgalp;
    private BufferedImage ımage_bgalp;
    private BufferedImage ımage_bos;
    private boolean isRunning;
    private Timer timer;
    private Timer foodTimer; // Yemi yavaşlatmak için bir zamanlayıcı
    private Point bullet;
    private char bulletDirection;
    private char bulletDirectiondik;
    private Timer bulletTimer;
    private int barYOffset = 10; // Çubukların yükseklik ofseti
    private char lastHorizontalDirection;
    private char lastVerticalDirection;

    public Sinavicin() throws IOException {

        this.zombiler = new String[level];

        subDirection = ' ';
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);

        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);
        ArrayList<String> bullets = new ArrayList<>();

        ımage = ImageIO.read(new FileImageInputStream(new File("arka.png")));
        ımage_asker = ImageIO.read(new FileImageInputStream(new File("asker.png")));
        try {
            BufferedImage bufferedImage = ImageIO.read(new File("zombie3.png"));
            ımage_zombie = bufferedImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        ımage_dabanca = ImageIO.read(new FileImageInputStream(new File("dabanca.png")));
        ımage_door = ImageIO.read(new FileImageInputStream(new File("door.png")));
        ımage_galp = ImageIO.read(new FileImageInputStream(new File("galp.png")));
        ımage_zgalp = ImageIO.read(new FileImageInputStream(new File("zgalp.png")));
        asker = new ArrayList<>();
        asker.add(new Point(WIDTH / 2, HEIGHT / 2));
        direction = ' ';
        directiondik = ' ';
        isRunning = true;
        zombiePositions = new HashSet<>();
        placeFood();
        placeWeapon();// Silahı yerleştir

        bullet = new Point(-CELL_SIZE, -CELL_SIZE); // Başlangıçta mermiyi ekranın dışına yerleştir
        bulletDirection = 'R'; // Başlangıçta mermi yönü belirsiz

        timer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isRunning) {
                    move();
                    checkCollision();
                    repaint();
                }
            }
        });
        timer.start();

        // Yemi yavaşlatmak için bir zamanlayıcı oluştur
        foodTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveFood();

                moveboss();

                repaint();
            }
        });
        foodTimer.start();
        bulletTimer = new Timer(1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bullets != null) {
                    moveBullets();
                    repaint();
                }

            }
        });
        bulletTimer.start();

    }

    public static void playSound(String soundFilePath) {
        File soundFile = new File(soundFilePath);

        try {
            // Ses dosyasını yükle
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);

            // Ses formatını al
            AudioFormat format = audioStream.getFormat();

            // Ses veri çizgisini oluştur
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            // Ses çizgisini aç
            Clip audioClip = (Clip) AudioSystem.getLine(info);
            audioClip.open(audioStream);

            // Ses dosyasını çal
            audioClip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    private void move() {
        Point head = asker.get(0);
        Point newHead = new Point(head);
        asker.add(0, newHead);
        if (direction == 'U') {
            newHead.y -= 2;// Sola hareket
        } else if (direction == 'D') {
            newHead.y += 2; // Sağa hareket
        }
        if (directiondik == 'L') {
            newHead.x -= 2; // Yukarı hareket
        } else if (directiondik == 'R') {
            newHead.x += 2;// Aşağı hareket
        }

        asker.remove(asker.size() - 1);
        check();

        // Yılanın yeni başını ekle
    }

    private void check() {
        Point head = asker.get(0);
        Point newHead = new Point(head);
        asker.add(0, newHead);

        int bulletCenterX = newHead.x + 1; // Mermi merkez noktasının X koordinatı
        int bulletCenterY = newHead.y + 1; // Mermi merkez noktasının Y koordinatı
        for (int i = 0; i < zombiler.length; i++) {
            String cell = zombiler[i];
            String[] zombiebilgileri = cell.split(",");
            int x = Integer.parseInt(zombiebilgileri[0]);
            int y = Integer.parseInt(zombiebilgileri[1]);
            int can = Integer.parseInt(zombiebilgileri[2]);

            zombie.x = x;
            zombie.y = y;
            int foodX = zombie.x;
            int foodY = zombie.y;
            Point newPosition = new Point(foodX, foodY);
            int zombieCenterX = zombie.x + (CELL_SIZE / 2); // Zombie merkez noktasının X koordinatı
            int zombieCenterY = zombie.y + (CELL_SIZE / 2); // Zombie merkez noktasının Y koordinatı
            double distance = Math.sqrt(Math.pow(bulletCenterX - zombieCenterX, 2) + Math.pow(bulletCenterY - zombieCenterY, 2));
            if (distance < CELL_SIZE / 2) {

                CAN--;
                if (foodX < head.x) {
                    foodX -= CELL_SIZE;
                } else if (foodX > head.x) {
                    foodX += CELL_SIZE;
                }

                if (foodY < head.y) {
                    foodY -= CELL_SIZE;
                } else if (foodY > head.y) {
                    foodY += CELL_SIZE;
                }
                zombie.setLocation(foodX, foodY);
                zombiePositions.add(newPosition);
                zombiler[i] = foodX + "," + foodY + "," + can;
                if (CAN == 0) {
                    gameOver(); // Yılan yemi yedikten sonra oyunu sonlandır
                    return;
                }

            }

        }

        if (can != null) {
            int canCenterX = can.x + (CELL_SIZE / 2); // Zombie merkez noktasının X koordinatı
            int canCenterY = can.y + (CELL_SIZE / 2); // Zombie merkez noktasının Y koordinatı

            // Mermi ve zombie arasındaki mesafeyi hesapla
            double distancecan = Math.sqrt(Math.pow(bulletCenterX - canCenterX, 2) + Math.pow(bulletCenterY - canCenterY, 2));
            if (distancecan < CELL_SIZE / 2) {
                if (CAN < 3) {
                    CAN++;
                    this.can = new Point(-CELL_SIZE, -CELL_SIZE);

                }

            }
        }
        if (level2 % 10 == 0) {

            placeBoss();
            level2 = 1;

            int bossCenterX = BOSS.x + (50 / 2); // Zombie merkez noktasının X koordinatı
            int bossCenterY = BOSS.y + (50 / 2); // Zombie merkez noktasının Y koordinatı

            // Mermi ve zombie arasındaki mesafeyi hesapla
            double distanceboss = Math.sqrt(Math.pow(bulletCenterX - bossCenterX, 2) + Math.pow(bulletCenterY - bossCenterY, 2));
            if (distanceboss < 50 / 2) {

                CAN -= 2;
                placeBoss();

                if (CAN == 0) {
                    gameOver(); // Yılan yemi yedikten sonra oyunu sonlandır
                    return;
                }

            }
        }

        // Yılanın yemi yediği durumu kontrol et
        if (newHead.x < 0) {
            newHead.x = WIDTH - CELL_SIZE; // Haritanın solundan çıkıldıysa, sağ taraftan giriş yap
        } else if (newHead.x >= WIDTH) {
            newHead.x = 0; // Haritanın sağından çıkıldıysa, sol taraftan giriş yap
        }

        if (newHead.y < 0) {
            newHead.y = 0; // Haritanın üstünden çıkıldıysa, alt taraftan giriş yap
        } else if (newHead.y >= HEIGHT) {
            newHead.y = HEIGHT - CELL_SIZE; // Haritanın altından çıkıldıysa, üst taraftan giriş yap
        }
        // Yılanın kuyruğunu kısalt
        asker.remove(asker.size() - 1);

        // Silahın konumunu güncelle
        placeWeapon();
    }

    private boolean collisionDetected(Point bullet, int zombieX, int zombieY) {
        int bulletCenterX = bullet.x + 1; // Mermi merkez noktasının X koordinatı
        int bulletCenterY = bullet.y + 1; // Mermi merkez noktasının Y koordinatı

        int zombieCenterX = zombieX + (CELL_SIZE / 2); // Zombie merkez noktasının X koordinatı
        int zombieCenterY = zombieY + (CELL_SIZE / 2); // Zombie merkez noktasının Y koordinatı

        // Mermi ve zombie arasındaki mesafeyi hesapla
        double distance = Math.sqrt(Math.pow(bulletCenterX - zombieCenterX, 2) + Math.pow(bulletCenterY - zombieCenterY, 2));

        // Eğer mesafe mermi çapından küçükse, çarpışma var demektir
        return distance < CELL_SIZE / 2;
    }

    private void checkCollision() {
        Point head = asker.get(0);

        // Check collision with walls
        // Check self-collision
        for (int i = 1; i < asker.size(); i++) {
            if (head.equals(asker.get(i))) {
                gameOver();
                return;
            }
        }

        // Check collision with weapon
        if (head.equals(weapon)) {
            // Weapon collision detected, handle it here
            // For now, let's just remove the weapon
            weapon.setLocation(-CELL_SIZE, -CELL_SIZE);
        }

    }

    private void moveFood() {
        zombiePositions.clear();
        for (int i = 0; i < zombiler.length; i++) {

            String cell = zombiler[i];
            String[] zombiebilgileri = cell.split(",");
            int x = Integer.parseInt(zombiebilgileri[0]);
            int y = Integer.parseInt(zombiebilgileri[1]);
            int can = Integer.parseInt(zombiebilgileri[2]);
            zombie.x = x;
            zombie.y = y;

            int foodX = zombie.x;
            int foodY = zombie.y;

            // Yılanın başının konumunu al
            Point head = asker.get(0);
            Point newPosition = new Point(foodX, foodY);
            if (!zombiePositions.contains(newPosition)) {

                // Yeminin yeni konumunu belirle
                // Yemi yılanın konumuna doğru hareket ettir
                if (foodX < head.x) {
                    foodX += 1;
                } else if (foodX > head.x) {
                    foodX -= 1;
                }

                if (foodY < head.y) {
                    foodY += 1;
                } else if (foodY > head.y) {
                    foodY -= 1;
                }
                if (CAN == 0) {
                    gameOver();
                    return;
                }
                // Yeni yemin konumunu ayarla
                zombie.setLocation(foodX, foodY);
                zombiePositions.add(newPosition);
                zombiler[i] = foodX + "," + foodY + "," + can;
            } else {
                if (foodX < head.x) {
                    foodX -= CELL_SIZE;
                } else if (foodX > head.x) {
                    foodX += CELL_SIZE;
                }

                if (foodY < head.y) {
                    foodY -= CELL_SIZE;
                } else if (foodY > head.y) {
                    foodY += CELL_SIZE;
                }
                zombie.setLocation(foodX, foodY);
                zombiePositions.add(newPosition);
                zombiler[i] = foodX + "," + foodY + "," + can;
            }

        }

    }

    private void moveboss() {
        if (BOSS == null) {
            return; // BOSS null ise hareket etmeye gerek yok
        }
        int bossX = BOSS.x;
        int bossY = BOSS.y;

        // Yılanın başının konumunu al
        Point head = asker.get(0);

        // Yeminin yeni konumunu belirle
        // Yemi yılanın konumuna doğru hareket ettir
        Point newPosition = new Point(bossX, bossY);
        if (!zombiePositions.contains(newPosition)) {
            zombiePositions.add(newPosition);
            if (bossX < head.x) {
                bossX += 1;
            } else if (bossX > head.x) {
                bossX -= 1;
            }

            if (bossY < head.y) {
                bossY += 1;
            } else if (bossY > head.y) {
                bossY -= 1;
            }
            if (CAN == 0) {
                gameOver();
                return;
            }
        }
        // Yeni yemin konumunu ayarla
        BOSS.setLocation(bossX, bossY);

    }

    private void placeFood() {
        for (int i = 0; i < level; i++) {
            Random random = new Random();
            int x;
            int y;

            boolean yer = random.nextBoolean();
            if (yer) {
                x = 500;
                y = 0;
                zombie = new Point(x, y);
                CAN_ZOMBİE = 3;
            } else {
                x = 500;
                y = 730;
                zombie = new Point(x, y);
                CAN_ZOMBİE = 3;
            }
            String val = x + "," + y + "," + CAN_ZOMBİE;
            zombiler[i] = val;

        }

    }

    private void placeBoss() {
        Random random = new Random();

        boolean yer = random.nextBoolean();
        if (yer) {
            int x = 0;
            int y = 365;
            BOSS = new Point(x, y);
            CAN_BOSS = 10;
        } else {
            int x = 970;
            int y = 365;
            BOSS = new Point(x, y);
            CAN_BOSS = 10;
        }

    }

    private void placeWeapon() {
        // Silahın boyutunu ve konumunu ayarla
        Point head = asker.get(0);
        int weaponX = head.x;
        int weaponY = head.y;

        // Silahın boyutunu yılanın boyutuna göre ayarla
        int weaponWidth = (int) (CELL_SIZE * 0.5);
        int weaponHeight = (int) (CELL_SIZE * 0.3);

        // Silahın konumunu yılanın dönüş yönüne göre ayarla
        if (direction == 'U') {
            weaponY -= CELL_SIZE;
        } else if (direction == 'D') {
            weaponY += CELL_SIZE;
        }
        if (directiondik == 'L') {
            weaponX -= CELL_SIZE;
        } else if (directiondik == 'R') {
            weaponX += CELL_SIZE;
        } else {
            weaponX += CELL_SIZE;
        }

        // Silahın ekranın dışına çıkmasını engelle
        weaponX = Math.min(Math.max(0, weaponX), WIDTH - weaponWidth);
        weaponY = Math.min(Math.max(0, weaponY), HEIGHT - weaponHeight);

        weapon = new Point(weaponX, weaponY);
    }

    private void shoot() {
        String ses = "silahsesiwav.wav";
        playSound(ses);

        // Asker listesi boş değilse ve en az bir asker varsa işlemi gerçekleştir
        // Silahın başı (yılanın başı) pozisyonunu al
        Point head = asker.get(0);
        int x = head.x + 5;
        int y = head.y + 5;

        // Mermiyi silahın bakış yönüne göre konumlandır ve yönünü ayarla
        // Yönü ayarla
        bulletDirection = direction;
        bulletDirectiondik = directiondik;

        String val = x + "," + y + "," + bulletDirection + "," + bulletDirectiondik;
        if (bulletDirection == ' ' && bulletDirectiondik == ' ') {
            val = x + "," + y + "," + ' ' + "," + 'R';
        }
        if (this.bullets == null) {
            this.bullets = new ArrayList<>();
        }
        // Burada bullets listesine eleman eklemeye devam edersiniz
        this.bullets.add(val);

        // Zamanlayıcıyı başlat
    }

//    private void shootgun() {
//        String ses = "silahsesiwav.wav";
//        playSound(ses);
//
//        // Asker listesi boş değilse ve en az bir asker varsa işlemi gerçekleştir
//        // Silahın başı (yılanın başı) pozisyonunu al
//        Point head = asker.get(0);
//        int x = head.x + 5;
//        int y = head.y + 5;
//
//        // Mermiyi silahın bakış yönüne göre konumlandır ve yönünü ayarla
//        // Yönü ayarla
//        double[] angles = {0, Math.PI / 4, Math.PI / 2, 3 * Math.PI / 4, Math.PI, -3 * Math.PI / 4, -Math.PI / 2, -Math.PI / 4};
//        for (double angle : angles) {
//            bullets.add(new Bullet(head.getX(), head.getY(), angle));
//        
//            }
//
//
//        String val = x + "," + y + "," + bulletDirection + "," + bulletDirectiondik;
//        if (bulletDirection == ' ' && bulletDirectiondik == ' ') {
//            val = x + "," + y + "," + ' ' + "," + 'R';
//        }
//        if (this.bullets == null) {
//            this.bullets = new ArrayList<>();
//        }
//        // Burada bullets listesine eleman eklemeye devam edersiniz
//        this.bullets.add(val);
//
//        // Zamanlayıcıyı başlat
//    }

//    public void longPressShoot() {
//        Point head = asker.get(0);
//        int weaponX = head.x;
//        int weaponY = head.y;
//        // Yılanın bakış açısında 8 farklı yöne mermi atışı gerçekleştir
//        double[] angles = {0, Math.PI / 4, Math.PI / 2, 3 * Math.PI / 4, Math.PI, -3 * Math.PI / 4, -Math.PI / 2, -Math.PI / 4};
//        for (double angle : angles) {
//            bullets.add(new Bullet(weaponX, weaponY, angle));
//        
//    
//
//        }
//    }
    private void moveBullets() {
        if (bullets != null) {
            for (int i = 0; i < bullets.size(); i++) {
                String cell = bullets.get(i);
                String[] zombiebilgileri = cell.split(",");
                int x = Integer.parseInt(zombiebilgileri[0]);
                int y = Integer.parseInt(zombiebilgileri[1]);
                String bulletDirection2 = zombiebilgileri[2];
                String bulletDirectiondik = zombiebilgileri[3];

                int foodX = x;
                int foodY = y;

                // Yılanın başının konumunu al
                Point head = asker.get(0);
                Point newPosition = new Point(foodX, foodY);
                if (bulletDirection2.equals("U")) {
                    foodY -= 2;
                } else if (bulletDirection2.equals("D")) {
                    foodY += 2;
                }
                if (bulletDirectiondik.equals("L")) {
                    foodX -= 2;
                } else if (bulletDirectiondik.equals("R")) {
                    foodX += 2;
                }

                // Yeni konumu diziye yaz
                String val = foodX + "," + foodY + "," + bulletDirection2 + "," + bulletDirectiondik;
                bullets.set(i, val);

                if (foodX < 0 || foodX >= WIDTH || foodY < 0 || foodY >= HEIGHT) {

                    bullets.remove(i);

                }

                if (BOSS != null) {
                    checkbossCollision();
                }
                // Mermi duvara veya yılana çarptıysa durdur ve ekrandan kaldır
                checkFoodCollision();
            }
        }

    }

    private boolean checkBulletCollision(Point bullet) {
        // Mermiyi yılan veya duvarlara çarptı mı kontrol et
        Point head = asker.get(0);

        // Duvarlarla çarpışma kontrolü
        if (bullet.x < 0 || bullet.x >= WIDTH || bullet.y < 0 || bullet.y >= HEIGHT) {
            return true; // Duvara çarpan mermi var
        }

        // Yılanla çarpışma kontrolü
        // Eğer yılanın başıyla mermi aynı noktadaysa, yani çarpışma olmuşsa
        if (bullet.equals(head)) {
            return true; // Yılanla çarpışma var
        }

        return false; // Herhangi bir çarpışma yok
    }

    private void checkFoodCollision() {
        for (int i = 0; i < bullets.size(); i++) {
            String cell = bullets.get(i);
            String[] zombiebilgileri = cell.split(",");
            if (zombiebilgileri.length >= 3) { // Dizinin boyutu 3'ten büyük veya eşit olduğunda işlem yap
                int x1 = Integer.parseInt(zombiebilgileri[0]);
                int y1 = Integer.parseInt(zombiebilgileri[1]);

                bullet.x = x1;
                bullet.y = y1;

                for (int j = 0; j < zombiler.length; j++) {
                    String cell1 = zombiler[j];
                    String[] zombiebilgileri1 = cell1.split(",");
                    int x = Integer.parseInt(zombiebilgileri1[0]);
                    int y = Integer.parseInt(zombiebilgileri1[1]);
                    int can = Integer.parseInt(zombiebilgileri1[2]);

                    zombie.x = x;
                    zombie.y = y;

                    // Mermi ve zombie'nin merkez noktalarının koordinatlarını al
                    int bulletCenterX = bullet.x + 1; // Mermi merkez noktasının X koordinatı
                    int bulletCenterY = bullet.y + 1; // Mermi merkez noktasının Y koordinatı
                    int zombieCenterX = zombie.x + (CELL_SIZE / 2); // Zombie merkez noktasının X koordinatı
                    int zombieCenterY = zombie.y + (CELL_SIZE / 2); // Zombie merkez noktasının Y koordinatı

                    // Mermi ve zombie arasındaki mesafeyi hesapla
                    double distance = Math.sqrt(Math.pow(bulletCenterX - zombieCenterX, 2) + Math.pow(bulletCenterY - zombieCenterY, 2));

                    // Eğer mermi ve zombie arasındaki mesafe, mermi ve zombie'nin boyutları dikkate alınarak belirlenen bir eşik değerden küçükse, çarpışma var demektir
                    if (distance < CELL_SIZE / 2) {

                        can--; // Zombie'nin canını azalt
                        zombiler[j] = zombieCenterX + "," + zombieCenterY + "," + can;
                        bullets.remove(i);

                        if (can == 0) {
                            if (level2 == 5) {
                                this.can = new Point(x, y);
//                        pleacecan();

//                        placeFood();
                            }
                            int silinecekIndeks = j;
                            ArrayList<String> yeniListe = new ArrayList<>(Arrays.asList(zombiler));
                            yeniListe.remove(silinecekIndeks);

                            zombiler = yeniListe.toArray(String[]::new);

                            zombieCenterX = -40;
                            zombieCenterY = -40;

                            oldurulen++;
                            if (zombiler.length == 0) {
                                level++;
                                this.zombiler = new String[level];
                                placeFood();
                            }

                            level2++;

                            // Yeni yemi yerleştir
                        }
                    }
                }
            }
        }
    }

    private void checkbossCollision() {
        for (int i = 0; i < bullets.size(); i++) {
            String cell = bullets.get(i);
            String[] zombiebilgileri = cell.split(",");
            if (zombiebilgileri.length >= 3) { // Dizinin boyutu 3'ten büyük veya eşit olduğunda işlem yap
                int x = Integer.parseInt(zombiebilgileri[0]);
                int y = Integer.parseInt(zombiebilgileri[1]);

                bullet.x = x;
                bullet.y = y;
                // Mermi ve zombie'nin merkez noktalarının koordinatlarını al
                int bulletCenterX = bullet.x + 1; // Mermi merkez noktasının X koordinatı
                int bulletCenterY = bullet.y + 1; // Mermi merkez noktasının Y koordinatı
                if (BOSS != null) {
                    int bossCenterX = BOSS.x + (50 / 2); // Zombie merkez noktasının X koordinatı
                    int bossCenterY = BOSS.y + (50 / 2); // Zombie merkez noktasının Y koordinatı

                    // Mermi ve zombie arasındaki mesafeyi hesapla
                    double distance = Math.sqrt(Math.pow(bulletCenterX - bossCenterX, 2) + Math.pow(bulletCenterY - bossCenterY, 2));

                    // Eğer mermi ve zombie arasındaki mesafe, mermi ve zombie'nin boyutları dikkate alınarak belirlenen bir eşik değerden küçükse, çarpışma var demektir
                    if (distance < 50 / 2) {
                        bullets.remove(i);

                        CAN_BOSS--; // Zombie'nin canını azalt
                        if (CAN_BOSS == 0) {
                            oldurulen++;
                            level++;
                            level2++;
                            BOSS = null;

                        }
                    }
                }
            }
        }
    }
    //    private void pleacecan() {
    //        for (String cell : zombiler) {
    //            String[] zombiebilgileri = cell.split(",");
    //            int x = Integer.parseInt(zombiebilgileri[0]);
    //            int y = Integer.parseInt(zombiebilgileri[1]);
    //            CAN_ZOMBİE = Integer.parseInt(zombiebilgileri[2]);
    //
    //            zombie.x = x;
    //            zombie.y = y;
    //
    //            int sayi = level2 % 5;
    //            if (sayi == 0) {
    //                x = zombie.x;
    //                y = zombie.y;
    //                can = new Point(x, y);
    //
    //            }
    //        }
    //    }

    private void gameOver() {
        isRunning = false;
        timer.stop();
        foodTimer.stop(); // Yemi yavaşlatmak için zamanlayıcıyı durdur
        JOptionPane.showMessageDialog(this, "Game Over!\nkilled zombies: " + oldurulen + "\nulaşılan level: " + level, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(ımage, 0, 0, ımage.getWidth() + 400, ımage.getHeight() + 400, this);
        // Food
        for (int i = 0; i < zombiler.length; i++) {
            String cell = zombiler[i];
            String[] zombiebilgileri = cell.split(",");
            if (zombiebilgileri.length >= 3) { // Dizinin boyutu 3'ten büyük veya eşit olduğunda işlem yap
                int x = Integer.parseInt(zombiebilgileri[0]);
                int y = Integer.parseInt(zombiebilgileri[1]);
                CAN_ZOMBİE = Integer.parseInt(zombiebilgileri[2]);

                zombie.x = x;
                zombie.y = y;

                g.drawImage(ımage_zombie, zombie.x, zombie.y, CELL_SIZE, CELL_SIZE, this);
                //System.out.println("Zombie bilgileri eksik: " + cell);
            } else {
                // Dizinin boyutu 3'ten küçükse, işlem yapma veya hata ayıklama
                System.out.println("Zombie bilgileri eksik: " + cell);
            }
        }
        if (bullets != null) {
            for (int i = 0; i < bullets.size(); i++) {
                String cell = bullets.get(i);
                if (cell != null) {
                    String[] zombiebilgileri = cell.split(",");
                    // Dizinin boyutu 3'ten büyük veya eşit olduğunda işlem yap
                    int x = Integer.parseInt(zombiebilgileri[0]);
                    int y = Integer.parseInt(zombiebilgileri[1]);

                    bullet.x = x;
                    bullet.y = y;
                    g.setColor(Color.WHITE);
                    g.fillRect(bullet.x, bullet.y, 3, 3);
                } else {
                    // Dizinin boyutu 3'ten küçükse, işlem yapma veya hata ayıklama
                    System.out.println("Zombie bilgileri eksik: " + cell + i);
                }
            }
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Killed Zombies: " + oldurulen, 10, 30);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("level: " + level, 900, 30);
        if (BOSS != null) {

            g.drawImage(ımage_zombie, BOSS.x, BOSS.y, CELL_SIZE + 10, CELL_SIZE + 10, this);
        }
        if (can != null) {

            g.drawImage(ımage_galp, can.x, can.y, CELL_SIZE, CELL_SIZE, this);
        }

        // Weapon
        // Silahın boyutu ve konumu güncellendi
        g.drawImage(ımage_dabanca, weapon.x, weapon.y, (int) (CELL_SIZE * 0.5), (int) (CELL_SIZE * 0.3), this);
        // Bullet
        // Örnek olarak 2x2 boyutunda bir mermi çizdim, istediğiniz boyuta göre ayarlayabilirsiniz

        for (int i = 0; i < asker.size(); i++) {
            Point p = asker.get(i);

            g.drawImage(ımage_asker, p.x, p.y, CELL_SIZE, CELL_SIZE, this);

        }

        // Draw door
        g.drawImage(ımage_door, DOOR_X, DOOR_Y_TOP, DOOR_WIDTH, DOOR_HEIGHT, this);

        g.drawImage(ımage_door, DOOR_X, DOOR_Y_BOTTOM, DOOR_WIDTH, DOOR_HEIGHT, this);

        // Draw health bars
        for (int i = 0; i < CAN; i++) {

            g.drawImage(ımage_galp, asker.get(0).x + (i * (barWidth + 5)), asker.get(0).y - barYOffset - barHeight, barWidth, barHeight, this);
        }
        for (String cell : zombiler) {
            String[] zombiebilgileri = cell.split(",");
            int x = Integer.parseInt(zombiebilgileri[0]);
            int y = Integer.parseInt(zombiebilgileri[1]);
            CAN_ZOMBİE = Integer.parseInt(zombiebilgileri[2]);

            zombie.x = x;
            zombie.y = y;

            for (int i = 0; i < CAN_ZOMBİE; i++) {

                g.drawImage(ımage_zgalp, zombie.x + (i * (barWidth + 5)), zombie.y - barYOffset - barHeight, barWidth, barHeight, this);
            }
        }
        if (BOSS != null) {
            int barX = BOSS.x + 5;
            int barY = BOSS.y - barYOffset - barHeight;

// Bir can çubuğu yerleştirin
            g.setColor(Color.GREEN);
            g.fillRect(barX, barY, barWidth * CAN_BOSS, barHeight);
            g.drawImage(ımage_zgalp, BOSS.x, BOSS.y - barYOffset - barHeight, barWidth, barHeight, this);
        }

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            direction = 'U';
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            direction = 'D';
        } else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            directiondik = 'L';
        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            directiondik = 'R';
        } else if (keyCode == KeyEvent.VK_M) {
            new Thread(() -> {
                while (true) {
                    shoot(); // Mermiyi ateşle
                    try {
                        // Her atış arasında kısa bir bekleme süresi ekleyerek mermiler arasında boşluk oluşturabiliriz
                        // Bu, mermilerin birbirine çok yakın olmasını önler ve daha okunaklı bir oyun deneyimi sunar
                        Thread.sleep(1000); // Mermi arasındaki bekleme süresi (milisaniye cinsinden)
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
            // M tuşuna basıldığında ateş et
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Implementasyon gerekli değil, bu metodu boş bırakabilirsiniz
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {

            bulletj++;
            shoot(); // Mermiyi ateşle

        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Implementasyon gerekli değil, bu metodu boş bırakabilirsiniz
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Implementasyon gerekli değil, bu metodu boş bırakabilirsiniz
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Implementasyon gerekli değil, bu metodu boş bırakabilirsiniz
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Oyuncu 1 hareketi
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            direction = ' ';
//            directiondik = ' ';

        } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            directiondik = ' ';
//            direction = ' ';// Dur
        }
        if (keyCode == KeyEvent.VK_M) {
            new Thread(() -> {
                while (true) {
                    shoot(); // Mermiyi ateşle
                    try {
                        // Her atış arasında kısa bir bekleme süresi ekleyerek mermiler arasında boşluk oluşturabiliriz
                        // Bu, mermilerin birbirine çok yakın olmasını önler ve daha okunaklı bir oyun deneyimi sunar
                        Thread.sleep(100); // Mermi arasındaki bekleme süresi (milisaniye cinsinden)
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
            // M tuşuna basıldığında ateş et
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) throws IOException {
        String soundFilePath = "sürgü.wav";

        // Ses dosyasını çal
        playSound(soundFilePath);

        JFrame frame = new JFrame("zombi kapmaca ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.getContentPane().add(new Sinavicin(), BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}
