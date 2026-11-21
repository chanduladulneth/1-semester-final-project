package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
import com.dcroyal.util.Session;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * OperatorDashboardController.java
 * Controller specifically for the Printer Operator Dashboard view.
 */
public class OperatorDashboardController {

    @FXML private HBox kpiBox;
    @FXML private HBox chartBox;

    @FXML
    public void initialize() {
        loadKpis();
        loadBarChart();
        loadLineChart();
    }

    private void loadKpis() {
        try {
            Connection con = DBConnection.getConnection();
            int userId = Session.getCurrentUser().getUserId();

            // KPI 1 — Jobs in Queue
            String sql1 = "SELECT COUNT(*) FROM orders WHERE order_status = 'PRINT_QUEUE'";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            int inQueue = rs1.getInt(1);
            rs1.close();
            ps1.close();

            // KPI 2 — Currently Printing
            String sql2 = "SELECT COUNT(*) FROM orders WHERE order_status = 'PRINTING'";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            int printing = rs2.getInt(1);
            rs2.close();
            ps2.close();

            // KPI 3 — My Assigned Jobs
            String sql3 = "SELECT COUNT(*) FROM orders WHERE assigned_printer_id = ?";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ps3.setInt(1, userId);
            ResultSet rs3 = ps3.executeQuery();
            rs3.next();
            int myJobs = rs3.getInt(1);
            rs3.close();
            ps3.close();

            // KPI 4 — Completed (Printed)
            String sql4 = "SELECT COUNT(*) FROM orders WHERE order_status = 'PRINTED_READY_FOR_PACKING'";
            PreparedStatement ps4 = con.prepareStatement(sql4);
            ResultSet rs4 = ps4.executeQuery();
            rs4.next();
            int completed = rs4.getInt(1);
            rs4.close();
            ps4.close();

            kpiBox.getChildren().addAll(
                createKpiCard("In Print Queue", String.valueOf(inQueue), "Waiting to print", inQueue > 0 ? "#F5B800" : "#1A7A46"),
                createKpiCard("Currently Printing", String.valueOf(printing), "Being printed now", "#07768F"),
                createKpiCard("My Assigned Jobs", String.valueOf(myJobs), "Assigned to me", "#07768F"),
                createKpiCard("Printed Done", String.valueOf(completed), "Ready for packing", "#1A7A46")
            );

        } catch (SQLException e) {
            System.out.println("Operator KPI error: " + e.getMessage());
        }
    }

    private void loadBarChart() {
        try {
            Connection con = DBConnection.getConnection();

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Status");
            yAxis.setLabel("Orders");

            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Print Jobs by Status");
            barChart.setLegendVisible(false);
            barChart.setAnimated(false);

            XYChart.Series<String, Number> series = new XYChart.Series<>();

            String sql = "SELECT order_status, COUNT(*) as cnt FROM orders GROUP BY order_status";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                        formatStatus(rs.getString("order_status")),
                        rs.getInt("cnt")
                ));
            }
            rs.close();
            ps.close();

            barChart.getData().add(series);
            styleBarChart(barChart);
            chartBox.getChildren().add(barChart);

        } catch (SQLException e) {
            System.out.println("Operator bar chart error: " + e.getMessage());
        }
    }

    private void loadLineChart() {
        try {
            Connection con = DBConnection.getConnection();

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Day");
            yAxis.setLabel("Orders");

            LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Print Jobs Completed (Last 7 Days)");
            lineChart.setLegendVisible(false);
            lineChart.setAnimated(false);

            XYChart.Series<String, Number> series = new XYChart.Series<>();

            String sql = "SELECT DATE_FORMAT(created_at, '%a') as day, COUNT(*) as cnt " +
                         "FROM orders WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                         "GROUP BY DATE(created_at) ORDER BY DATE(created_at)";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("day"), rs.getInt("cnt")));
            }
            rs.close();
            ps.close();

            lineChart.getData().add(series);
            styleLineChart(lineChart);
            chartBox.getChildren().add(lineChart);

        } catch (SQLException e) {
            System.out.println("Operator line chart error: " + e.getMessage());
        }
    }

    private VBox createKpiCard(String title, String value, String note, String noteColor) {
        VBox card = new VBox(6);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #E6E0D3;" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 1);"
        );
        card.setMinWidth(150);
        card.setPrefWidth(200);
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #62656F; -fx-font-weight: 600;");
        titleLabel.setWrapText(true);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: #15171F;");

        Label noteLabel = new Label(note);
        noteLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: " + noteColor + ";");
        noteLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, valueLabel, noteLabel);
        return card;
    }

    private void styleBarChart(BarChart<String, Number> chart) {
        chart.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 10; -fx-border-color: #E6E0D3; -fx-border-radius: 14; -fx-border-width: 1;");
        chart.setMinWidth(260);
        chart.setMaxWidth(Double.MAX_VALUE);
        chart.setPrefHeight(300);
        HBox.setHgrow(chart, Priority.ALWAYS);
    }

    private void styleLineChart(LineChart<String, Number> chart) {
        chart.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 10; -fx-border-color: #E6E0D3; -fx-border-radius: 14; -fx-border-width: 1;");
        chart.setMinWidth(260);
        chart.setMaxWidth(Double.MAX_VALUE);
        chart.setPrefHeight(300);
        HBox.setHgrow(chart, Priority.ALWAYS);
    }

    private String formatStatus(String status) {
        if (status == null) return "";
        String[] words = status.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word.substring(1));
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }
}
