package gc.analise_de_malhas;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.*;

public class HelloController {

    private static class Malha {
        TextField resistores;
        TextField fontes;

        Malha(TextField r, TextField f) {
            resistores = r;
            fontes = f;
        }
    }

    @FXML
    private TextField quantidadeMalhas;
    @FXML
    private VBox boxMalhas;
    @FXML
    private VBox inputValores;
    @FXML
    private VBox painelEquacoes;
    @FXML
    private VBox painelResultados;   // <-- CORRIGIDO

    private List<Malha> malhas = new ArrayList<>();

    private List<Set<String>> resistoresPorMalha;
    private List<Map<String, Integer>> interfacesPorMalha;
    private List<List<String>> fontesPorMalha;

    private Set<String> todosComponentes;
    private Map<String, TextField> camposComponentes = new LinkedHashMap<>();

    @FXML
    private void gerarMalhas() {

        String entrada = quantidadeMalhas.getText();
        if (!entrada.matches("\\d+")) {
            boxMalhas.getChildren().clear();
            malhas.clear();
            return;
        }

        int n = Integer.parseInt(entrada);

        boxMalhas.getChildren().clear();
        malhas.clear();

        for (int i = 0; i < n; i++) {

            Label titulo = new Label("Malha " + (i + 1));
            titulo.getStyleClass().add("titulo-malha");

            TextField rMalha = new TextField();
            rMalha.setPromptText("Resistores (ex: R1 R2 R3)");

            TextField fMalha = new TextField();
            fMalha.setPromptText("Fontes (ex: +V1 -V2)");

            malhas.add(new Malha(rMalha, fMalha));
            boxMalhas.getChildren().addAll(titulo, rMalha, fMalha);
        }
    }

    @FXML
    private void InputValores() {

        resistoresPorMalha = new ArrayList<>();
        fontesPorMalha = new ArrayList<>();
        todosComponentes = new LinkedHashSet<>();

        inputValores.getChildren().clear();
        camposComponentes.clear();

        for (Malha m : malhas) {
            Set<String> r = parseResistores(m.resistores.getText());
            resistoresPorMalha.add(r);
            todosComponentes.addAll(r);

            List<String> fontes = parseFontes(m.fontes.getText());
            fontesPorMalha.add(fontes);

            for (String f : fontes) {
                todosComponentes.add(f.replace("+", "").replace("-", ""));
            }
        }

        interfacesPorMalha = gerarInterfacesAutomaticas(resistoresPorMalha);

        for (String nome : todosComponentes) {
            Label lbl = new Label(nome);
            TextField campo = new TextField();
            campo.setPromptText("valor de " + nome);
            campo.getStyleClass().add("campo-valor");

            camposComponentes.put(nome, campo);
            inputValores.getChildren().addAll(lbl, campo);
        }
    }

    @FXML
    private void outputEquacoes() {

        painelEquacoes.getChildren().clear();
        painelResultados.getChildren().clear();

        int n = Integer.parseInt(quantidadeMalhas.getText());

        double[][] A = new double[n][n];
        double[] B = new double[n];

        Map<String, Double> valores = new HashMap<>();
        for (var e : camposComponentes.entrySet()) {
            String nome = e.getKey();
            String txt = e.getValue().getText();
            valores.put(nome, txt.isBlank() ? 0.0 : Double.parseDouble(txt));
        }

        for (int m = 0; m < n; m++) {

            double somaRes = resistoresPorMalha.get(m).stream().mapToDouble(valores::get).sum();

            A[m][m] = somaRes;

            StringBuilder eq = new StringBuilder();
            eq.append("Malha ").append(m + 1).append(":  ").append(String.format("(%.2f)i%d", somaRes, m + 1));

            for (var entry : interfacesPorMalha.get(m).entrySet()) {

                String res = entry.getKey();
                int outra = entry.getValue();
                double r = valores.get(res);

                A[m][outra] -= r;

                eq.append(String.format(" - (%.2f)i%d", r, outra + 1));
            }

            double somaFontes = 0;
            for (String f : fontesPorMalha.get(m)) {
                boolean neg = f.startsWith("-");
                String nome = f.replace("-", "").replace("+", "");
                double v = valores.get(nome);
                somaFontes += neg ? -v : v;
            }

            B[m] = somaFontes;

            eq.append(" = ").append(String.format("%.2f", somaFontes));

            TextField campoEq = new TextField(eq.toString());
            campoEq.getStyleClass().add("campo-equacao");

            painelEquacoes.getChildren().add(campoEq);
        }
    }

    @FXML
    private void resolverSistema() {

        int n = Integer.parseInt(quantidadeMalhas.getText());

        double[][] A = new double[n][n];
        double[] B = new double[n];

        painelResultados.getChildren().clear();

        Map<String, Double> valores = new HashMap<>();
        for (var e : camposComponentes.entrySet()) {
            valores.put(e.getKey(), Double.parseDouble(e.getValue().getText()));
        }

        for (int m = 0; m < n; m++) {

            double somaRes = resistoresPorMalha.get(m).stream().mapToDouble(valores::get).sum();

            A[m][m] = somaRes;

            for (var entry : interfacesPorMalha.get(m).entrySet()) {
                A[m][entry.getValue()] -= valores.get(entry.getKey());
            }

            double somaFontes = 0;
            for (String f : fontesPorMalha.get(m)) {
                boolean neg = f.startsWith("-");
                String nome = f.replace("-", "").replace("+", "");
                double v = valores.get(nome);
                somaFontes += neg ? -v : v;
            }

            B[m] = somaFontes;
        }

        double[] x = resolverSistemaInterno(A, B);

        for (int i = 0; i < x.length; i++) {
            Label lbl = new Label("I" + (i + 1) + " = " + String.format("%.4f A", x[i]));
            lbl.getStyleClass().add("resultado-corrente");
            painelResultados.getChildren().add(lbl);
        }
    }

    private double[] resolverSistemaInterno(double[][] A, double[] B) {

        int n = B.length;

        for (int i = 0; i < n; i++) {

            double pivo = A[i][i];
            if (pivo == 0) pivo = 1e-9;

            for (int j = i; j < n; j++)
                A[i][j] /= pivo;

            B[i] /= pivo;

            for (int k = i + 1; k < n; k++) {
                double fator = A[k][i];
                for (int j = i; j < n; j++)
                    A[k][j] -= fator * A[i][j];
                B[k] -= fator * B[i];
            }
        }

        double[] x = new double[n];

        for (int i = n - 1; i >= 0; i--) {
            x[i] = B[i];
            for (int j = i + 1; j < n; j++)
                x[i] -= A[i][j] * x[j];
        }

        return x;
    }

    public static Set<String> parseResistores(String r) {
        Set<String> set = new LinkedHashSet<>();
        for (String s : r.trim().split("\\s+"))
            if (!s.isEmpty()) set.add(s);
        return set;
    }

    private static List<String> parseFontes(String f) {
        List<String> lista = new ArrayList<>();
        for (String s : f.trim().split("\\s+"))
            if (!s.isEmpty()) lista.add(s);
        return lista;
    }

    private Map<String, List<Integer>> mapearResistores(List<Set<String>> resistores) {

        Map<String, List<Integer>> mapa = new LinkedHashMap<>();

        for (int i = 0; i < resistores.size(); i++) {
            for (String r : resistores.get(i)) {
                mapa.computeIfAbsent(r, k -> new ArrayList<>()).add(i);
            }
        }
        return mapa;
    }

    private List<Map<String, Integer>> gerarInterfacesAutomaticas(List<Set<String>> resistoresPorMalha) {

        int n = resistoresPorMalha.size();
        List<Map<String, Integer>> interfaces = new ArrayList<>();

        for (int i = 0; i < n; i++)
            interfaces.add(new LinkedHashMap<>());

        Map<String, List<Integer>> aparicoes = mapearResistores(resistoresPorMalha);

        for (var entry : aparicoes.entrySet()) {

            String res = entry.getKey();
            List<Integer> malhas = entry.getValue();

            if (malhas.size() > 1) {
                for (int i = 0; i < malhas.size(); i++) {
                    for (int j = i + 1; j < malhas.size(); j++) {

                        int m1 = malhas.get(i);
                        int m2 = malhas.get(j);

                        interfaces.get(m1).put(res, m2);
                        interfaces.get(m2).put(res, m1);
                    }
                }
            }
        }
        return interfaces;
    }
}
