package com.darwinruiz.hospital.utils;

import java.util.ArrayList;
import java.util.List;

public class TableFormatter {
    
    private final List<String> headers;
    private final List<List<String>> rows;
    private final List<Integer> columnWidths;
    
    public TableFormatter() {
        this.headers = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.columnWidths = new ArrayList<>();
    }

    public TableFormatter setHeaders(String... headers) {
        this.headers.clear();
        this.columnWidths.clear();
        
        for (String header : headers) {
            this.headers.add(header);
            this.columnWidths.add(header.length());
        }
        return this;
    }

    public TableFormatter addRow(String... values) {
        List<String> row = new ArrayList<>();
        
        for (int i = 0; i < values.length && i < headers.size(); i++) {
            String value = values[i] != null ? values[i] : "";
            row.add(value);

            if (value.length() > columnWidths.get(i)) {
                columnWidths.set(i, value.length());
            }
        }

        while (row.size() < headers.size()) {
            row.add("");
        }
        
        rows.add(row);
        return this;
    }

    public String build() {
        if (headers.isEmpty()) {
            return "Tabla vacía";
        }
        
        StringBuilder sb = new StringBuilder();

        sb.append(createSeparatorLine());
        sb.append("\n");

        sb.append(createRowLine(headers));
        sb.append("\n");

        sb.append(createSeparatorLine());
        sb.append("\n");

        for (List<String> row : rows) {
            sb.append(createRowLine(row));
            sb.append("\n");
        }

        sb.append(createSeparatorLine());
        
        return sb.toString();
    }

    public void print() {
        System.out.println(build());
    }

    private String createSeparatorLine() {
        StringBuilder sb = new StringBuilder();
        sb.append("+");
        
        for (int width : columnWidths) {
            sb.append("-".repeat(width + 2));
            sb.append("+");
        }
        
        return sb.toString();
    }

    private String createRowLine(List<String> values) {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            int width = columnWidths.get(i);
            
            sb.append(" ");
            sb.append(padRight(value, width));
            sb.append(" |");
        }
        
        return sb.toString();
    }

    private String padRight(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return text + " ".repeat(width - text.length());
    }

    public TableFormatter clear() {
        rows.clear();
        return this;
    }

    public int getRowCount() {
        return rows.size();
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public static void printSimpleTable(String[] headers, List<String[]> data) {
        TableFormatter formatter = new TableFormatter();
        formatter.setHeaders(headers);
        
        for (String[] row : data) {
            formatter.addRow(row);
        }
        
        formatter.print();
    }

    public static void printNoDataMessage(String mensaje) {
        System.out.println();
        System.out.println("┌" + "─".repeat(mensaje.length() + 2) + "┐");
        System.out.println("│ " + mensaje + " │");
        System.out.println("└" + "─".repeat(mensaje.length() + 2) + "┘");
        System.out.println();
    }
}