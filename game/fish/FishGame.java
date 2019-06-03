package org.wf.game.fish;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FishGame {
    /**
     * @param args
     * @version ����1.0
     * @time 2019.06.00
     */
    public static void main(String[] args) {
        //��Ϸ����
        JFrame jf = new JFrame("�����");
        jf.setSize(800, 480);
        jf.setLocationRelativeTo(null);
        jf.setResizable(false);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setAlwaysOnTop(true);
        //�������
        Pool pool = new Pool();
        jf.add(pool);
        //��ʾ���
        jf.setVisible(true);
        pool.action();
    }
}

//�����
class Pool extends JPanel {
    private static final long serialVersionUID = 1L;
    BufferedImage bgImage;    //����ͼƬ
    Fish[] fishs = new Fish[20];    //���е���
    Net net = new Net();    //����
    boolean isExit;    //����Ƿ�����Ϸ����
    int score, bullet = 50;    //��Ϸ�÷�,�ӵ���

    public Pool() {
        super();
        File bg = new File("images/bg.jpg");
        try {
            bgImage = ImageIO.read(bg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //0-8,9-17��Ӧ1-9����
        for (int i = 0; i < fishs.length / 2 - 1; i++) {
            fishs[i] = new Fish(i + 1);
            fishs[i + 9] = new Fish(i + 1);
        }
        fishs[18] = new Fish(10);
        fishs[19] = new Fish(11);
    }

    //����ϷԪ��
    @Override
    public void paint(Graphics g) {
        g.drawImage(bgImage, 0, 0, null);    //������
        for (Fish fish : fishs) {
            g.drawImage(fish.fishImage, fish.fish_x, fish.fish_y, null);    //����
        }
        if (!isExit) {
            g.drawImage(net.netImage, net.netX, net.netY, null);    //����
        }

        //����Ϸ˵������
        g.setColor(Color.GREEN);
        g.setFont(new Font("����", Font.ITALIC, 20));
        g.drawString("�����V1.0 By~Synchronized", 10, 25);
        g.drawString("�ӵ���:" + bullet + "   �÷�:" + score, 350, 25);
        g.drawString("�Ҽ��л�����  VIP:" + (net.power % 7 + 1), 590, 25);
        if (bullet <= 0) {
            g.setColor(Color.RED);
            g.setFont(new Font("����", Font.BOLD, 100));
            g.drawString("Game Over", 150, 250);
            bullet = 0;
            isExit = true;
            net.power = -1;
        }
    }

    //��Ϸ��������
    public void action() {
        for (Fish fish : fishs) {
            fish.start();
        }
        //��������
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int event = e.getModifiers();
                if (event == 4) {
                    net.change();    //�л�����С
                    super.mousePressed(e);
                } else if (event == 16) {
                    //���ӵ�
                    if (bullet - (net.power % 7 + 1) <= 0) {
                        bullet = 0;
                    } else {
                        bullet -= (net.power % 7 + 1);
                    }
                    //����
                    for (Fish fish : fishs) {
                        if (!fish.catched) {
                            catchFish(fish);
                        }
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                net.moveTo(e.getX(), e.getY());
                super.mouseMoved(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isExit = false;
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isExit = true;
                super.mouseExited(e);
            }
        };
        this.addMouseListener(adapter);
        this.addMouseMotionListener(adapter);
        while (true) {
            repaint();
        }
    }

    //��װ����ķ���
    public void catchFish(Fish fish) {
        fish.catched = net.catchFish(fish);
        if (fish.catched) {
            score += 2 * fish.k;
            bullet += 2 * fish.k;
        }
    }
}

//����
class Fish extends Thread {
    int fish_x, fish_y;    //�������
    BufferedImage fishImage;    //���ͼƬ
    BufferedImage[] fishImages = new BufferedImage[10];    //�㶯����ͼƬ
    BufferedImage[] catchImages;    //��ı�����ͼƬ
    int fish_width, fish_height;    //��Ŀ��
    Random r = new Random();    //��y����������
    int blood;    //���Ѫ��ֵ
    boolean catched;    //���Ƿ񱻲�
    int k, step_size;    //���Ѫ���ȼ�,�ƶ��ٶ�

    public Fish(int m) {
        super();
        String preName = m > 9 ? m + "" : "0" + m;
        //ͨ��forѭ����ȡ�㶯��ͼƬ����
        for (int i = 0; i < fishImages.length; i++) {
            int j = i + 1;
            String lastName = j > 9 ? "10" : "0" + j;
            File file = new File("images/fish" + preName + "_" + lastName + ".png");
            try {
                fishImages[i] = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fishImage = fishImages[0];
        fish_width = this.fishImage.getWidth();
        fish_height = this.fishImage.getHeight();
        fish_x = 800;
        fish_y = r.nextInt(450 - fish_height);
        blood = m * 3;
        this.k = m;
        step_size = r.nextInt(5) + 1;
        //��ʼ��catchImages
        if (m > 7) {
            catchImages = new BufferedImage[4];
        } else if (m <= 7) {
            catchImages = new BufferedImage[2];
        }
        //ͨ��forѭ����ȡ�㱻��ͼƬ����
        for (int i = 1; i <= catchImages.length; i++) {
            File file = new File("images/fish" + preName + "_catch_0" + i + ".png");
            try {
                catchImages[i - 1] = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //���ƶ��ķ���
    public void move() {
        fish_x -= step_size;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            move();    //�������ƶ��ķ���
            //��������,��������
            if (fish_x < -fish_width || catched) {
                turnOut();    //�㱻��,����
                newFish();
            }
            change();    //������ҡ���ζ��ķ���
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //��������һ����
    public void newFish() {
        fish_x = 800;
        fish_y = r.nextInt(450 - fish_height);
        if (fish_y >= 450) {
            // System.out.println(fish_height + "  " + fish_y);
        }
        catched = false;
        blood = k * 3;
        step_size = r.nextInt(5) + 1;
    }

    // ��ҡ���ζ��ķ���
    int index = 0;

    public void change() {
        index++;
        fishImage = fishImages[index / 3 % 10];
    }

    //�㱻�������ķ���
    public void turnOut() {
        for (int i = 0; i < catchImages.length; i++) {
            fishImage = catchImages[i];
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

//������
class Net {
    int netX, netY;//��������
    int netWidth, netHeight;//���Ŀ��
    BufferedImage netImage;//����ͼƬ

    public Net() {
        super();
        File file = new File("images/net_" + 1 + ".png");
        try {
            netImage = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        netX = 300;
        netY = 300;
        netWidth = netImage.getWidth();
        netHeight = netImage.getHeight();
    }

    // �����л��ķ���
    int power = 0;

    public void change() {
        power++;
        int x = this.netX + this.netWidth / 2;
        int y = this.netY + this.netHeight / 2;
        File file = new File("images/net_" + (power % 7 + 1) + ".png");
        try {
            netImage = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.netWidth = netImage.getWidth();
        this.netHeight = netImage.getHeight();
        moveTo(x, y);
    }

    //�����ƶ��ķ���
    public void moveTo(int x, int y) {
        this.netX = x - this.netWidth / 2;
        this.netY = y - this.netHeight / 2;
    }

    //����
    public boolean catchFish(Fish fish) {
        //������������
        int zX = netX + netWidth / 2;
        int zY = netY + netHeight / 2;
        //������岿����������
        int fX = fish.fish_x + fish.fish_width * 2 / 3;
        int fY = fish.fish_y + fish.fish_height / 2;
        //�����������������������岿��
        if (zX > fish.fish_x && zX < fish.fish_x + fish.fish_width * 2 / 3
                && zY > fish.fish_y && zY < fish.fish_y + fish.fish_height) {
            fish.blood -= ((power % 7 + 1) * 2);
            // System.out.println(fish.blood);
        } else if (fX > netX && fX < netX + netWidth
                && fY > netY && fY < netY + netHeight) {
            //���������岿����������������
            fish.blood -= ((power % 7 + 1) * 2);
            // System.out.println(fish.blood);
        }
        return fish.blood <= 0;
    }
}