package com.example.ruclinicgui;

import com.example.ruclinicgui.clinic.src.*;
import com.example.ruclinicgui.clinic.src.util.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Scanner;


public class ClinicManagerController implements Initializable {

    List<Appointment> appts = new List <>();
    List <Provider> providers = new List<>();
    CircularLinkedList technicians = new CircularLinkedList();
    Node pointer;
    List<Appointment> imagingAppts = new List<>();
    Sort sort = new Sort();
    ListMethods methods = new ListMethods();

    @FXML
    private DatePicker appointmentDatePicker;

    public ClinicManagerController() throws IOException {
    }

    @FXML
    private Date getDateSelected() {
        LocalDate selectedDate = appointmentDatePicker.getValue();
        String formattedDate;

        if (selectedDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            formattedDate = selectedDate.format(formatter);
        } else {
            System.out.println("No date selected");
            return null;
        }
        if (!checkApptDate(formattedDate)) {
            return null;
        }
        return stringToDate(formattedDate);
    }

    public boolean checkApptDate(String input) {
        Date date = stringToDate(input);

        // Check if the date is valid
        if (!date.isValidDate()) {
            showAlert("Invalid Date", "Appointment date: " + input + " is not a valid calendar date.", Alert.AlertType.ERROR);
            return false;
        }
        else if (date.isBeforeToday() || date.isToday()) {
            showAlert("Invalid Appointment Date", "Appointment date: " + input + " is today or a date before today.", Alert.AlertType.WARNING);
            return false;
        }
        else if (date.onWeekend()) {
            showAlert("Weekend Appointment", "Appointment date: " + input + " is Saturday or Sunday.", Alert.AlertType.WARNING);
            return false;
        }
        else if (!date.isWithinSixMonths()) {
            showAlert("Out of Range", "Appointment date: " + input + " is not within six months.", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Optional: Remove if you want a header
        alert.setContentText(message);
        alert.showAndWait(); // Shows the alert and waits for the user to close it
    }

    @FXML
    private TextArea outputArea;

    @FXML
    private RadioButton option1;  // Corresponds to fx:id="option1" in FXML

    @FXML
    private RadioButton option2;  // Corresponds to fx:id="option2" in FXML

    @FXML
    private ToggleGroup chooseOne;  // Corresponds to fx:id="choiceGroup" in FXML

    @FXML
    public void initializeToggleButtons() {
        System.out.println("choiceGroup: " + chooseOne);
        option1.setToggleGroup(chooseOne);
        option2.setToggleGroup(chooseOne);
        chooseOne.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            RadioButton selectedButton = (RadioButton) newValue;
            if (selectedButton == option1) {
                System.out.println("Option 1 is selected");
            } else if (selectedButton == option2) {
                System.out.println("Option 2 is selected");
            } else {
                System.out.println("No option is selected");
            }
        });
    }

    @FXML
    private DatePicker dobDatePicker;

    @FXML
    private TextField fname;

    @FXML
    private TextField lname;

    @FXML
    private Person getPatient() {
        LocalDate selectedDate = dobDatePicker.getValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String formattedDate = selectedDate.format(formatter);
        Date date = stringToDate(formattedDate);
        if(!checkDOB(date)){
            return null;
        }
        Profile patientProfile = new Profile(fname.getText(), lname.getText(), stringToDate(formattedDate));
        return new Person(patientProfile);
    }

    @FXML
    private Timeslot getTimeslot() {
        String slotString = chooseTimeslot.getValue();
        Timeslot slot = new Timeslot();
        slot.setTimeslot(slotString);
        return slot;
    }

    @FXML
    private Provider getProvider() {
        String selectedProvider = chooseProvider.getValue();
        System.out.println("Selected Provider: " + selectedProvider);
        Provider output = null;
        for (Provider provider : providers) {
            if (provider.toString().equals(selectedProvider)) {
                output = provider;
            }
        }
        return output;
    }


    @FXML
    private void schedule() {
        if (getDateSelected() == null || getTimeslot() == null || getPatient() == null || getProvider() == null) {
            showAlertForSchedule("Missing Information", "Please fill out all required fields.");
            return;
        }

        if (getTypeOfAppointment(chooseOne).equals("D")) {
            Appointment newAppt = new Appointment(getDateSelected(), getTimeslot(), getPatient(), getProvider());
            appts.add(newAppt);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String formattedDate = appointmentDatePicker.getValue().format(formatter);
            outputArea.appendText(formattedDate + " " + getTimeslot().toString() + " " + getPatient().toString() + " " + getProvider().toString() + " booked.");
        } else if (getTypeOfAppointment(chooseOne).equals("T")) {
            // Handle other appointment types if needed
        }
    }

    private void showAlertForSchedule(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); // You can change the type to ERROR, WARNING, etc. as needed
        alert.setTitle(title);
        alert.setHeaderText(null); // You can set a header text if needed
        alert.setContentText(message);
        alert.showAndWait(); // Show the dialog and wait for the user to close it
    }

    public boolean checkDOB(Date dob) {
        if (!dob.isValidDate()) {
            showAlertDOB("Invalid Date", "Patient DOB " + dob.toString() + " is not a valid calendar date.");
            return false;
        } else if (dob.isToday() || dob.isFutureDate()) {
            showAlertDOB("Invalid Date", "Patient DOB " + dob.toString() + " is today or a future date.");
            return false;
        } else {
            return true;
        }
    }

    private void showAlertDOB(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header
        alert.setContentText(message);
        alert.showAndWait(); // Display the alert and wait for the user to close it
    }

    @FXML
    private ChoiceBox<String> chooseTimeslot;

    @FXML
    private ChoiceBox<String> chooseProvider;

    @FXML
    private Text timeslot;

    @FXML
    private Button loadProvidersButton;

    public String getTypeOfAppointment(ToggleGroup radioGroup) {
        // Get the selected toggle from the group and cast it to a RadioButton
        Toggle selectedToggle =radioGroup.getSelectedToggle();
        if (selectedToggle instanceof RadioButton) {
            RadioButton selectedRadioButton = (RadioButton) selectedToggle;
            // Check which RadioButton is selected and return the corresponding string
            if (selectedRadioButton == option1) {
                return "D";
            } else if (selectedRadioButton == option2) {
                return "T";
            }
        }
        return "No option is selected";  // Return this if no RadioButton is selected
    }


    private final String[] times = {"9:00 AM", "9:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "2:00 PM", "2:30 PM", "3:00 PM", "3:30 PM", "4:00 PM", "4:30 PM"};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chooseTimeslot.getItems().addAll(times);
        outputArea.setEditable(false);
    }

    @FXML
    protected void onLoadProvidersClick() {
        // Create a FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Provider File");

        // Set extension filters to show .txt files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog
        File file = fileChooser.showOpenDialog(loadProvidersButton.getScene().getWindow());

        if (file != null) {
            // Validate the file extension
            if (file.getName().endsWith(".txt")) {
                System.out.println("File selected: " + file.getAbsolutePath());

                // Call the loadProviders method with the selected file
                System.out.println("Went to loadProviders");
                System.out.println(option1);
                System.out.println(option2);
                System.out.println(chooseOne);
                initializeToggleButtons();
                loadProviders(file);
                printProviders();

                loadProvidersButton.setDisable(true);
            } else {
                // Invalid file type
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid File Type");
                alert.setHeaderText(null);
                alert.setContentText("Please select a valid text file (.txt).");
                alert.showAndWait();
            }
        } else {
            timeslot.setText("File selection canceled."); // turn this into modal
        }
    }

    @FXML
    protected void onScheduleClick() {
        System.out.println("Schedule button clicked");
        schedule();
    }

    public void loadProviders(File file) {
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }

        try (Scanner scanner = new Scanner(file)) { // Using try-with-resources to automatically close the scanner
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splittedLine = line.split("  ");
                if (splittedLine[0].equals("D")) {
                    Profile profile = new Profile(splittedLine[1], splittedLine[2], stringToDate(splittedLine[3]));
                    Specialty specialty = setSpecialty(splittedLine[5]);
                    Doctor doctor = new Doctor(profile, setLocation(splittedLine[4]), specialty, splittedLine[6]);
                    providers.add(doctor);
                } else if (splittedLine[0].equals("T")) {
                    Profile profile = new Profile(splittedLine[1], splittedLine[2], stringToDate(splittedLine[3]));
                    Location location = setLocation(splittedLine[4]);
                    int rate = Integer.parseInt(splittedLine[5]);
                    Technician technician = new Technician(profile, location, rate);
                    providers.add(technician);
                    technicians.addTechnician(technician);
                }
            }
            pointer = technicians.getHead();

            // Populate the ChoiceBox with provider names
            for (Provider provider : providers) {
                System.out.println(getTypeOfAppointment(chooseOne));
                if (getTypeOfAppointment(chooseOne).equals("D")) {
                    if (provider instanceof Doctor) {
                        chooseProvider.getItems().add(provider.toString());
                    }
                }
                else if (getTypeOfAppointment(chooseOne).equals("T")) {
                    if (provider instanceof Technician) {
                        chooseProvider.getItems().add(provider.toString());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Providers loaded to the list.");
    }

    public void printProviders() {
        sort.sortByProvider(providers); // check if this is the right syntax
        for (int i = 0; i<providers.size(); i++) {
            System.out.println(providers.get(i).toString());
        }
        technicians.display();
    }

    public Date stringToDate(String date) {
        String[] dateString = date.split("/");

        if (dateString.length != 3) {
            throw new IllegalArgumentException("Invalid date format. Expected format: MM/DD/YYYY");
        }
        int month = Integer.parseInt(dateString[0]);
        int day = Integer.parseInt(dateString[1]);
        int year = Integer.parseInt(dateString[2]);

        Date dateObject = new Date(year, month, day);

        if (dateObject == null) {
            return null;
        }
        else {
            return dateObject;
        }
    }

    public Specialty setSpecialty(String input) {
        Specialty specialty;
        if (input.equals("FAMILY")) {
            return Specialty.Family;
        }
        else if (input.equals("PEDIATRICIAN")) {
            return Specialty.Pediatrician;
        }
        else if (input.equals("ALLERGIST")) {
            return Specialty.Allergist;
        }
        return null;
    }

    public Location setLocation(String input) {
        Location location;
        if (input.equals("BRIDGEWATER")) {
            return Location.Bridgewater;
        }
        else if (input.equals("CLARK")) {
            return Location.Clark;
        }
        else if (input.equals("PRINCETON")) {
            return Location.Princeton;
        }
        else if (input.equals("PISCATAWAY")) {
            return Location.Piscataway;
        }
        else if (input.equals("MORRISTOWN")) {
            return Location.Morristown;
        }
        else if (input.equals("EDISON")) {
            return Location.Edison;
        }
        return null;
    }
}