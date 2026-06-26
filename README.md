# Sistema de Gestão de Academia (Planos e Treinos)

Sistema desktop em Java (Swing) com persistência em banco de dados SQLite via JDBC.
Arquitetura em camadas: `model` (entidades), `dao` (acesso a dados/SQL), `util`
(conexão, validações e exceções customizadas) e `ui` (telas Swing).

## ⚠️ Passo obrigatório antes de rodar: baixar o driver JDBC do SQLite

O arquivo `lib/sqlite-jdbc.jar` **não está incluído** neste pacote (download de
binários externos foi bloqueado no ambiente onde o projeto foi gerado). Você
precisa baixá-lo manualmente — é rápido:

1. Acesse: https://github.com/xerial/sqlite-jdbc/releases
2. Baixe o arquivo `.jar` da versão mais recente (ex: `sqlite-jdbc-3.45.1.0.jar`)
3. Coloque o arquivo dentro da pasta `lib/` deste projeto
4. Renomeie o arquivo para `sqlite-jdbc.jar` (ou ajuste o nome nos passos abaixo
   de configuração do Build Path / Project Structure)

Alternativa: baixe direto pelo Maven Central, mesmo link de busca:
https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/

## Configuração no Eclipse

1. `File > Import > Existing Projects into Workspace`, selecione a pasta `academia`
2. O projeto já vem com `.project` e `.classpath` configurados, apontando para
   `lib/sqlite-jdbc.jar`
3. Depois de colocar o jar na pasta `lib/`, clique com botão direito no projeto
   → `Refresh` (F5). Se o Eclipse não reconhecer automaticamente, vá em
   `Build Path > Configure Build Path > Libraries > Add JARs` e selecione o jar
4. Rode a classe `academia.ui.TelaPrincipal` (botão direito → `Run As > Java Application`)

## Configuração no IntelliJ IDEA

1. `Open`, selecione a pasta `academia` (o projeto já tem `.idea/` configurado)
2. Depois de colocar o jar em `lib/sqlite-jdbc.jar`, vá em
   `File > Project Structure > Modules > Dependencies` e confirme que a
   biblioteca `sqlite-jdbc` aparece apontando para o jar (já vem pré-configurada
   no `.iml`, só precisa o arquivo existir fisicamente)
3. Configure um JDK 17 ou superior em `Project Structure > Project > SDK`
4. Rode a classe `academia.ui.TelaPrincipal`

## Banco de dados

O sistema usa SQLite com um arquivo local `academia.db`, criado automaticamente
na primeira execução (na raiz onde o programa for executado). Todas as tabelas
são criadas automaticamente pelo método `ConexaoBD.criarTabelas()` — não é
necessário rodar nenhum script SQL manualmente.

Tabelas criadas: `aluno`, `plano`, `matricula`, `ficha_treino`, `historico_ficha`, `presenca`.

## Estrutura do projeto

```
academia/
├── lib/                          (coloque aqui o sqlite-jdbc.jar)
├── src/main/java/academia/
│   ├── model/                    Aluno, Plano, Matricula, FichaTreino, HistoricoFicha, Presenca
│   ├── dao/                      AlunoDAO, PlanoDAO, MatriculaDAO, FichaTreinoDAO, PresencaDAO
│   ├── util/                     ConexaoBD, ValidadorCpf, exceções customizadas
│   └── ui/                       TelaPrincipal e demais telas Swing
├── .project / .classpath         configuração Eclipse
├── .idea/ / *.iml                configuração IntelliJ
└── academia.db                   criado automaticamente na 1ª execução
```

## Funcionalidades implementadas

**CRUDs**
- Alunos: CPF (com validação de dígitos verificadores), nome, data de nascimento,
  telefone, e-mail, observações/restrições (opcional)
- Planos: código único, nome, valor (> 0), duração em dias (> 0), benefícios

**Matrícula e vínculo de plano**
- Tela de Matrícula: seleciona aluno + plano, define data de início e calcula
  automaticamente o vencimento (início + duração do plano)
- Bloqueia matrícula duplicada se o aluno já tiver uma matrícula ativa

**Montagem de treino**
- Fichas A/B/C/D/E por aluno, com exercícios e metas em texto livre
- Histórico de alterações: toda vez que uma ficha é salva, é gravado um snapshot
  no histórico (consulte pelo botão "Ver Histórico de Alterações")

**Controle de presença**
- Check-in diário por aluno
- Bloqueia check-in se não houver matrícula ativa ou se o plano estiver vencido
  (regra revalidada no momento do clique, exibindo o motivo do bloqueio)
- Impede check-in duplicado no mesmo dia

**Renovação/Cancelamento**
- Renovação: soma a duração do plano à data de vencimento atual (ou à data de
  hoje, se já estiver vencido), com confirmação antes de salvar
- Cancelamento: exige motivo (campo obrigatório) e confirmação explícita

**Relatórios (todos com filtros configuráveis na tela)**
1. Planos ativos e vencendo nos próximos X dias
2. Presenças por período (lista detalhada + resumo de fluxo diário)
3. Alunos inativos sem check-in nos últimos X dias
4. Receita por plano somada em um período (data de início da matrícula)
5. Treinos por aluno (fichas cadastradas e última atualização)
6. Cancelamentos por período, com motivos
7. Horários de pico (quantidade de check-ins por faixa horária 00h-23h)

## Observações técnicas

- Datas são armazenadas como texto no formato ISO (AAAA-MM-DD) no banco,
  e exibidas como dd/MM/aaaa nas telas
- O sistema usa `PRAGMA foreign_keys = ON`, então não é possível excluir um
  aluno ou plano que já tenha matrícula, ficha ou presença vinculada — a
  exclusão será bloqueada pelo próprio banco (e a tela mostra uma mensagem
  amigável explicando isso)
- Ao abrir o sistema (TelaPrincipal), todas as matrículas ATIVA cujo
  vencimento já passou são automaticamente marcadas como VENCIDA
