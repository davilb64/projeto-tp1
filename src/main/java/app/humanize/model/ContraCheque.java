package app.humanize.model;

import java.time.LocalDate;

public class ContraCheque {
        private final String nomeFuncionario;
        private final LocalDate dataEmissao;
        private final double totalProventos;
        private final double totalDescontos;
        private double saldo;

        public ContraCheque(String nomeFuncionario, LocalDate dataEmissao, double totalProventos, double totalDescontos, double saldo) {
            this.nomeFuncionario = nomeFuncionario;
            this.dataEmissao = dataEmissao;
            this.totalProventos = totalProventos;
            this.totalDescontos = totalDescontos;
            this.saldo = saldo;
        }

        public String getNomeFuncionario() { return nomeFuncionario; }

        public LocalDate getDataEmissao() { return dataEmissao; }

        public double getTotalProventos() { return totalProventos; }

        public double getTotalDescontos() { return totalDescontos; }

        public double getSaldo() { return saldo; }
        public void setSaldo(double saldo) { this.saldo = saldo; }
    }
