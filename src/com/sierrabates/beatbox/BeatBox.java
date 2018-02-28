package com.sierrabates.beatbox;

import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;

public class BeatBox {

    private ArrayList<JCheckBox> checkboxList;
    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;
    private JFrame theFrame;
    private JLabel tempoLabel;

    private String[] instrumentNames = {"Bass Drum", "Low-mid Tom", "Closed Hi-Hat", // перечень имен инструментов
            "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "Vibraslap", "High Agogo",
            "Open Hi Conga"};
    private int[] instruments = {35, 47, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 67, 63};  // перечень номеров инструментов


    public static void main(String[] args) {   // запускаем программу
        new BeatBox().buildGUI();               // создаем объект, вызываем метод создания интерфейса
    }

    private void buildGUI() {        // метод создания интерфейса
        Box buttonBox = new Box(BoxLayout.Y_AXIS);              // контейнер для кнопок

        JButton startButton = new JButton("Start");         // создаем кнопку "Start"
        startButton.addActionListener(new MyStartListener());   // добавляем ей слушателя нажатия
        buttonBox.add(startButton);                             // добавляем в контейнер

        JButton stopButton = new JButton("Stop");           // тоже самое с кнопкой "Stop"
        stopButton.addActionListener(new MyStopListener());
        buttonBox.add(stopButton);

        JLabel emptyLabel_01 = new JLabel(" ");
        buttonBox.add(emptyLabel_01);

        JButton upTempoButton = new JButton("Tempo Up");    // с кнопкой "Tempo Up"
        upTempoButton.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempoButton);

        tempoLabel = new JLabel("Текуший темп: 120");
        buttonBox.add(tempoLabel);

        JButton downTempoButton = new JButton("Tempo Down");// с кнопкой "Tempo Down"
        downTempoButton.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempoButton);

        JLabel emptyLabel_02 = new JLabel(" ");
        buttonBox.add(emptyLabel_02);

        JButton saveButton = new JButton("Save");         // создаем кнопку "Save"
        saveButton.addActionListener(new MySaveListener());   // добавляем ей слушателя нажатия
        buttonBox.add(saveButton);                             // добавляем в контейнер

        JButton loadButton = new JButton("Load");           // тоже самое с кнопкой "Load"
        loadButton.addActionListener(new MyLoadListener());
        buttonBox.add(loadButton);

        Box nameBox = new Box(BoxLayout.Y_AXIS);                // контейнер для надписей имен инструментов
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));         // добавляем в контейнер надписи из массива
        }

        GridLayout grid = new GridLayout(16, 16);      // сеточный диспетчер компоновки
        grid.setVgap(1);                                        // задаем вертикальный промежуток между элементами
        grid.setHgap(2);                                        // задаем горизонтальный промежуток между элементами
        JPanel mainPanel = new JPanel(grid);

        checkboxList = new ArrayList<>();              // ArrayList с чекбоксами
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
        theFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//  устанавливаем выход по закрытию
        theFrame.getContentPane().add(background);              // устанавливаем фоновую панель в качестве ContentPane
        theFrame.setBounds(650, 200, 630, 500);    // устанавливаем координаты окна и его размеры
        theFrame.pack();            // подгоняем размеры окна в соответствии с размерами и положением компонентов
        theFrame.setVisible(true);                              // делаем окно видимым

        setUpMidi();                    // создаем миди систему
    } // close method


    private void setUpMidi() {       // метод создания миди системы
        try {
            sequencer = MidiSystem.getSequencer();              // создаем синтезатор
            sequencer.open();                                   // открываем его
            sequence = new Sequence(Sequence.PPQ, 4);  //создаем последовательность
        } catch (Exception e) {         // обрабатываем исключение
            e.printStackTrace();        // выводим трассировку стека
        }
    } // close method

    private void buildTrackAndStart() {  // метод сборки и запуска трека
        sequence.deleteTrack(track);    // удаляем старый трек из последовательности
        track = sequence.createTrack(); // создаем новый трек
        // проверяем состояние флажков, если установлен, добавляем в трек
        for (int i = 0; i < 16; i++) {          // перебираем строки с инструментами
            for (int j = 0; j < 16; j++) {      // перебираем такты в строке
                if (checkboxList.get(j + (16 * i)).isSelected()) {  //если для этого инструмента в этом такте флажок установлен
                    track.add(makeEvent(144, 9, instruments[i], 100, j));           // добавляем вкл. ноты в трек
                    track.add(makeEvent(128, 9, instruments[i], 100, j + 1));   // добавляем выкл. ноты в трек
                }
            } // close inner loop
            track.add(makeEvent(176, 1, 127, 0, 16));   // вот че здесь добавляем не пойму
        } // close outer

        track.add(makeEvent(192, 9, 1, 0, 15));         // и здесь че делаем нипанятна
        try {
            sequencer.setSequence(sequence);                    // устанавливаем последовательность в синтезатор (сформированный трек находится в ней)
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);// за loop_ливаем воспроизведение
            sequencer.start();                                  // запускаем
            int tempo = 120;
            sequencer.setTempoInBPM(tempo);                     // задаем темп
            tempoLabel.setText("Текущий теммп: " + tempo);      // выводим текущий темп
        } catch (Exception e) {         // обрабатываем исключение
            e.printStackTrace();        // выводим трассировку стека
        }
    } // close buildTrackAndStart method

    private MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) { // метод формирования миди события
        MidiEvent event = null;     // создаем пустое событие
        try {
            ShortMessage a = new ShortMessage();    // создаем короткое сообщение
            a.setMessage(comd, chan, one, two);     // задаем ему параметры
            event = new MidiEvent(a, tick);         // вставляем сообщение в событие в нужный такт

        } catch (Exception e) {     // обрабатываем исключение
            e.printStackTrace();    // выводим трассировку стека
        }
        return event;       // возврвщаем событие
    }

    public class MyStartListener implements ActionListener {    // класс обработчик нажатия на кнопку "Start"
        public void actionPerformed(ActionEvent a) {            // переопределенный метод обработчика
            buildTrackAndStart();                               // вызываем сборку и запуск трека
        }
    } // close inner class

    public class MyStopListener implements ActionListener {     // класс обработчик нажатия на кнопку "Stop"
        public void actionPerformed(ActionEvent a) {            // переопределенный метод обработчика
            sequencer.stop();                                   // останавливаем воспроизведение
        }
    } // close inner class

    public class MyUpTempoListener implements ActionListener {      // класс обработчик нажатия на кнопку увеличения темпа
        public void actionPerformed(ActionEvent a) {                // переопределенный метод обработчика
            float tempoFactor = sequencer.getTempoFactor();         // получаем текущий темп
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));  // увеличиваем его значение и устанавливаем
            tempoLabel.setText("Текущий теммп: " +
                    (int) (sequencer.getTempoInBPM() * tempoFactor));   // выводим текущий темп
        }
    } // close inner class

    public class MyDownTempoListener implements ActionListener {    // класс обработчик нажатия на кнопку снижения темпа
        public void actionPerformed(ActionEvent a) {                // переопределенный метод обработчика
            float tempoFactor = sequencer.getTempoFactor();         // получаем текущий темп
            sequencer.setTempoFactor((float) (tempoFactor * .97));  // снижаем его значение и устанавливаем
            tempoLabel.setText("Текущий теммп: " +
                    (int) (sequencer.getTempoInBPM() * tempoFactor));   // выводим текущий темп
        }
    } // close inner class

    public class MySaveListener implements ActionListener {    // класс обработчик нажатия на кнопку "Save"
        public void actionPerformed(ActionEvent a) {            // переопределенный метод обработчика
            boolean[] arrayChBx = new boolean[256];         //

            for (int i = 0; i<checkboxList.size(); i++)
                arrayChBx[i] = checkboxList.get(i).isSelected();
            JFileChooser fch = new JFileChooser("C:\\Users\\Stormcoder\\Documents\\Java\\SierraBates\\BeatBox");
            fch.showSaveDialog(theFrame);
            try {
                FileOutputStream fileOS = new FileOutputStream(fch.getSelectedFile());
                ObjectOutputStream os = new ObjectOutputStream(fileOS);
                os.writeObject(arrayChBx);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } // close inner class

    public class MyLoadListener implements ActionListener {    // класс обработчик нажатия на кнопку "Load"
        public void actionPerformed(ActionEvent a) {            // переопределенный метод обработчика
            boolean[] arrayChBx = new boolean[256];
            JFileChooser fch = new JFileChooser("C:\\Users\\Stormcoder\\Documents\\Java\\SierraBates\\BeatBox");
            fch.showOpenDialog(theFrame);
            try {
                FileInputStream fileIS = new FileInputStream(fch.getSelectedFile());
                ObjectInputStream is = new ObjectInputStream(fileIS);
                arrayChBx = (boolean[]) is.readObject();
                is.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            int i = 0;
            for (JCheckBox box : checkboxList) {
                if (arrayChBx[i]) box.setSelected(true);
                i++;
            }
        }
    } // close inner class
} // close class

















