import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

public class ToDoListApp {

    private JFrame frame;
    private DefaultListModel<TaskItem> listModel;
    private DefaultListModel<TaskItem> doneListModel; // Separate list model for done tasks
    private JList<TaskItem> toDoList;
    private JList<TaskItem> doneList; // JList for done tasks
    private JTextField taskInput;

    private static final String FILENAME = "todolist.txt"; // Name of the file to save tasks
    // Add a JComboBox for selecting the sorting option
    private JComboBox<String> viewOptionComboBox;

    // Add a button to trigger the view operation
    private JButton viewButton;
    public ToDoListApp() {
        // Create the main frame
        frame = new JFrame("To-Do List Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 300); // Adjust frame width to accommodate two lists

        // Create a list model to hold tasks
        listModel = new DefaultListModel<>();
        doneListModel = new DefaultListModel<>(); // Initialize the doneListModel

        // Create a JList component to display the tasks
        toDoList = new JList<>(listModel);
        doneList = new JList<>(doneListModel); // JList for done tasks

        // Set the font for the JList
        toDoList.setFont(new Font("Arial", Font.PLAIN, 24)); // Change the font and size as needed
        doneList.setFont(new Font("Arial", Font.PLAIN, 24)); // Font for done tasks

        // Create a text field for task input
        taskInput = new JTextField(20);

        // Add an ActionListener to the text field to add tasks when Enter key is pressed
        taskInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });




        // Create a button to add tasks
        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });

        // Create a button to remove selected tasks
        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedTasks();
            }
        });

        // Create a button to mark selected tasks as done
        JButton markAsDoneButton = new JButton("Mark as Done");
        markAsDoneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markSelectedTasksAsDone();
            }
        });

        // Create an "Exit" button
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTasksToFile();
                System.exit(0);
            }
        });



        // Create a panel for input components
        JPanel inputPanel = new JPanel();
        inputPanel.add(taskInput);
        inputPanel.add(addButton);
        inputPanel.add(removeButton);
        inputPanel.add(markAsDoneButton);
        inputPanel.add(exitButton);

        // Create a panel for the task lists with BorderLayout
        JPanel listPanel = new JPanel(new BorderLayout());

        // Set preferred sizes for the task list displays
        listPanel.setPreferredSize(new Dimension(400, 200)); // Adjust dimensions as needed

        JScrollPane toDoListScrollPane = new JScrollPane(toDoList);
        JScrollPane doneListScrollPane = new JScrollPane(doneList);

        // Add the lists to the panel side by side
        JPanel listsPanel = new JPanel(new GridLayout(1, 2));
        listsPanel.add(toDoListScrollPane);
        listsPanel.add(doneListScrollPane);

        listPanel.add(listsPanel, BorderLayout.CENTER);

        // Load tasks from the file
        loadTasksFromFile();

        // Add the input and list panels to the main frame
        frame.getContentPane().add(inputPanel, BorderLayout.NORTH);
        frame.getContentPane().add(listPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
    private void viewDoneTasks() {
        // Clear the "To-Do" list
        listModel.clear();

        // Iterate through the "Done" tasks and add them to the "To-Do" list
        for (int i = 0; i < doneListModel.getSize(); i++) {
            TaskItem task = doneListModel.getElementAt(i);
            listModel.addElement(task);
        }
    }
    private void addTask() {
        String taskDescription = taskInput.getText();
        if (!taskDescription.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String dateTime = dateFormat.format(new Date());
            TaskItem task = new TaskItem(dateTime, taskDescription, false);
            listModel.addElement(task);
            taskInput.setText("");
            saveTasksToFile();
        }
    }

    private void removeSelectedTasks() {
        int[] selectedIndices = toDoList.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            listModel.removeElementAt(selectedIndices[i]);
        }
        saveTasksToFile();
    }

    //    private void markSelectedTasksAsDone() {
//        int[] selectedIndices = toDoList.getSelectedIndices();
//        for (int index : selectedIndices) {
//            TaskItem task = listModel.getElementAt(index);
//            task.setDone(true);
//            listModel.set(index, task);
//        }
//        saveTasksToFile();
//    }
// Add a new constant for the filename of the "done" tasks
    private static final String DONE_FILENAME = "donetasks.txt";

    // Modify the markSelectedTasksAsDone method to save the task to both lists and files
    private void markSelectedTasksAsDone() {
        int[] selectedIndices = toDoList.getSelectedIndices();
        for (int index : selectedIndices) {
            TaskItem task = listModel.getElementAt(index);
            task.setDone(true);
            listModel.set(index, task);
            doneListModel.addElement(task); // Add the task to the doneListModel

            // Save the task to both files
            saveTasksToFile();
            saveDoneTasksToFile(task);
        }
    }

    // Create a new method to save "done" tasks to the "done tasks" file
    private void saveDoneTasksToFile(TaskItem task) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DONE_FILENAME, true))) {
            writer.println(task.getFormattedTask());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveTasksToFile() {
        try (PrintWriter writer = new PrintWriter(FILENAME)) {
            for (int i = 0; i < listModel.getSize(); i++) {
                TaskItem task = listModel.getElementAt(i);
                writer.println(task.getFormattedTask());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasksFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" - ");
                if (parts.length >= 2) {
                    String dateTime = parts[0];
                    String description = parts[1];
                    boolean isDone = false;

                    // Check if the task has the "Done" status
                    if (parts.length > 2 && parts[2].equalsIgnoreCase("Done")) {
                        isDone = true;
                    }

                    if (isDone) {
                        doneListModel.addElement(new TaskItem(dateTime, description, isDone));
                    } else {
                        listModel.addElement(new TaskItem(dateTime, description, isDone));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ToDoListApp();
            }
        });
    }

    private static class TaskItem {
        private String dateTime;
        private String description;
        private boolean done;

        public TaskItem(String dateTime, String description, boolean done) {
            this.dateTime = dateTime;
            this.description = description;
            this.done = done;
        }

        public String getFormattedTask() {
            return dateTime + " - " + description + (done ? " - Done" : "");
        }

        public void setDone(boolean done) {
            this.done = done;
        }

        @Override
        public String toString() {
            return getFormattedTask();
        }
    }
}
