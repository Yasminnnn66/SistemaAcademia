package academia.model;

import java.time.LocalDateTime;

/**
 * Registra um "snapshot" de uma ficha de treino sempre que ela é alterada,
 * permitindo exibir o histórico de alterações pedido na tela de Treinos.
 */
public class HistoricoFicha {

    private int id;
    private int fichaId;
    private String exercicios;
    private String metas;
    private LocalDateTime dataAlteracao;

    public HistoricoFicha() {
    }

    public HistoricoFicha(int id, int fichaId, String exercicios, String metas, LocalDateTime dataAlteracao) {
        this.id = id;
        this.fichaId = fichaId;
        this.exercicios = exercicios;
        this.metas = metas;
        this.dataAlteracao = dataAlteracao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFichaId() {
        return fichaId;
    }

    public void setFichaId(int fichaId) {
        this.fichaId = fichaId;
    }

    public String getExercicios() {
        return exercicios;
    }

    public void setExercicios(String exercicios) {
        this.exercicios = exercicios;
    }

    public String getMetas() {
        return metas;
    }

    public void setMetas(String metas) {
        this.metas = metas;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(LocalDateTime dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }
}
