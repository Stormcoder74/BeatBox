package com.sierrabates.beatbox;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class BeatBox_backup {

    JPanel mainPanel;
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;

    String[] instrumentNames = {"Bass Drum", "Low-mid Tom", "Closed Hi-Hat", // перечень имен инструментов
            "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "Vibraslap", "High Agogo",
            "Open Hi Conga"};
    int[] instruments = {35, 47, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 67, 63};  // перечень номеров инструментов


    public static void main(String[] args) {   // запускаем программу
        new BeatBox_backup().buildGUI();               // создаем объект, вызываем метод создания интерфейса
    }

    public void buildGUI() {        // метод создания интерфейса
        Box buttonBox = new Box(BoxLayout.Y_AXIS);              // контейнер для кнопок

        JButton startButton = new JButton("Start");         // создаем кнопку "Start"
        startButton.addActionListener(new MyStartListener());   // добавляем ей слушателя нажатия
        buttonBox.add(startButton);                             // добавляем в контейнер

        JButton stopButton = new JButton("Stop");           // тоже самое с кнопкой "Stop"
        stopButton.addActionListener(new MyStopListener());
        buttonBox.add(stopButton);

        JButton upTempoButton = new JButton("Tempo Up");    // с кнопкой "Tempo Up"
        upTempoButton.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempoButton);

        JButton downTempoButton = new JButton("Tempo Down");// с кнопкой "Tempo Down"
        downTempoButton.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempoButton);

        Box nameBox = new Box(BoxLayout.Y_AXIS);                // контейнер для надписей имен инструментов
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));         // добавляем в контейнер надписи из массива
        }

        GridLayout grid = new GridLayout(16, 16);      // сеточный диспетчер компоновки
        grid.setVgap(1);                                        // задаем вертикальный промежуток между элементами
        grid.setHgap(2);                                        // задаем горизонтальный промежуток между элементами
        mainPanel = new JPanel(grid);                           // создаем главную панель с этим диспетчером

        checkboxList = new ArrayList<JCheckBox>();              // ArrayList с чекбоксами
        for (int i = 0; i < 256; i++) {                         // создаем 256 чекбоксов
            JCheckBox c = new JCheckBox();      // создаем
            c.setSelected(false);               // сбрасываем
            checkboxList.add(c);                // добавляем в список
            mainPanel.add(c);                   // добавляем в главную панель
        } // end loop

        BorderLayout layout = new BorderLayout();               // создаем диспетчер компоновки BorderLayout
        JPanel background = new JPanel(layout);                 // создаем фоновую панель с этим диспетчером
        background.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10)); // создаем поля на панели
        background.add(BorderLayout.EAST, buttonBox);           // добавляем кнопки на восток фоновой панели
        background.add(BorderLayout.WEST, nameBox);             // надписи на запад
        background.add(BorderLayout.CENTER, mainPanel);         // главную панель в центр

        theFrame = new JFrame("Cyber BeatBox");     // создаем окно с заголовком
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//  устанавливаем выход по закрытию
        theFrame.getContentPane().add(background);              // устанавливаем фоновую панель в качестве ContentPane
        theFrame.setBounds(650, 200, 630, 500);    // устанавливаем координаты окна и его размеры
        theFrame.pack();            // подгоняем размеры окна в соответствии с размерами и положением компонентов
        theFrame.setVisible(true);                              // делаем окно видимым

        setUpMidi();                    // создаем миди систему
    } // close method


    public void setUpMidi() {       // метод создания миди системы
        try {
            sequencer = MidiSystem.getSequencer();              // создаем синтезатор
            sequencer.open();                                   // открываем его
            sequence = new Sequence(Sequence.PPQ, 4);  //создаем последовательность
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // close method

    public void buildTrackAndStart() {
        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new int[16];
            for (int j = 0; j < 16; j++) {
                if (checkboxList.get(j + (16 * i)).isSelected()) {
                    trackList[j] = instruments[i];
                } else {
                    trackList[j] = 0;
                }
            } // close inner loop

            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        } // close outer

        track.add(makeEvent(192, 9, 1, 0, 15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // close buildTrackAndStart method

    public void makeTracks(int[] list) {

        for (int i = 0; i < 16; i++) {
            int key = list[i];

            if (key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i + 1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            buildTrackAndStart();
        }
    } // close inner class

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            sequencer.stop();
        }
    } // close inner class

    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    } // close inner class

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * .97));
        }
    } // close inner class
} // close class