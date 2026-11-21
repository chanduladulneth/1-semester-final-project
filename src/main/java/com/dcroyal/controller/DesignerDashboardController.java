package com.dcroyal.controller;

import com.dcroyal.db.DBConnection;
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
 * DesignerDashboardController.java
 * Controller specifically for the Designer Dashboard view.
 */
public class DesignerDashboardController {

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

            // KPI 1 — Pending Designs
            String sql1 = "SELECT COUNT(*) FROM designs WHERE status = 'PENDING'";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            int pending = rs1.getInt(1);
            rs1.close();
            ps1.close();

            // KPI 2 — In Progress
            String sql2 = "SELECT COUNT(*) FROM designs WHERE status = 'IN_PROGRESS'";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            int inProgress = rs2.getInt(1);
            rs2.close();
            ps2.close();

            // KPI 3 — Approved
            String sql3 = "SELECT COUNT(*) FROM designs WHERE status = 'APPROVED'";
            PreparedStatement ps3 = con.prepareStatement(sql3);
            ResultSet rs3 = ps3.executeQuery();
            rs3.next();
            int approved = rs3.getInt(1);
            rs3.close();
            ps3.close();

            // KPI 4 — Total Designs
            String sql4 = "SELECT COUNT(*) FROM designs";
            PreparedStatement ps4 = con.prepareStatement(sql4);
            ResultSet rs4 = ps4.executeQuery();
            rs4.next();
            int totalDesigns = rs4.getInt(1);
            rs4.close();
            ps4.close();

            kpiBox.getChildren().addAll(
                createKpiCard("Pending", String.valueOf(pending), "Waiting to start", pending > 0 ? "#F5B800" : "#1A7A46"),
                createKpiCard("In Progress", String.valueOf(inProgress), "Being designed", "#07768F"),
                createKpiCard("Approved", String.valueOf(approved), "Ready for printing", "#1A7A46"),
                createKpiCard("Total Designs", String.valueOf(totalDesigns), "All time", "#07768F")
            );

        } catch (SQLException e) {
            System.out.println("Designer KPI error: " + e.getMessage());
        }
    }

    private void loadBarChart() {
        try {
            Connection con = DBConnection.getConnection();

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Print Type");
            yAxis.setLabel("Count");

            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Designs by Print Type");
            barChart.setLegendVisible(false);
            barChart.setAnimated(false);

            XYChart.Series<String, Number> series = new XYChart.Series<>();

            String sql = "SELECT print_category, COUNT(*) as cnt FROM designs GROUP BY print_category";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("print_category"), rs.getInt("cnt")));
            }
            rs.close();
            ps.close();

            barChart.getData().add(series);
            styleBarChart(barChart);
            chartBox.getChildren().add(barChart);

        } catch (SQLException e) {
            System.out.println("Designer bar chart error: " + e.getMessage());
        }
    }

    private void loadLineChart() {
        try {
            Connection con = DBConnection.getConnection();

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Day");
            yAxis.setLabel("Designs");

            LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Designs Created (Last 7 Days)");
            lineChart.setLegendVisible(false);
            lineChart.setAnimated(false);

            XYChart.Series<String, Number> series = new XYChart.Series<>();

            String sql = "SELECT DATE_FORMAT(created_at, '%a') as day, COUNT(*) as cnt " +
                         "FROM designs WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
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
            System.out.println("Designer line chart error: " + e.getMessage());
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
}
