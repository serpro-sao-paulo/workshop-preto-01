# EARS - GECON: Gerar Número de Contrato/Convênio

> Conversão proposta de `RN_Gecon.md`. O documento histórico foi preservado.
> Requisitos com `NEEDS-CLARIFICATION` mantêm a regra registrada, mas indicam a informação necessária para torná-la verificável.

REQ-GECON-001:
  pattern: ubiquitous
  text: "O sistema deverá disponibilizar os fluxos de inclusão, inclusão manual, alteração e consulta de número de contrato ou convênio."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/xml/AcaoNumeroContrato.xml#L27-L100
  original: "Gerar, alterar, consultar e cadastrar número de contrato/convênio não gerado pelo GECON."
  acceptance:
    - "O arquivo de configuração deverá mapear uma ação para cada um dos quatro fluxos."
    - "Cada ação deverá possuir uma tela de entrada e um encaminhamento de sucesso ou falha."
  priority: P0

REQ-GECON-002:
  pattern: ubiquitous
  text: "O sistema deverá apresentar os campos de unidade de lavratura, unidade gestora, unidade de lotação, nome do solicitante, telefone e observação no fluxo de inclusão."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/gera_numero_contrato1.jsp#L47-L137
  original: "As informações do fluxo Gerar Número de Contrato/Convênio devem obedecer às regras dos campos da RN2.2.1."
  acceptance:
    - "A tela de inclusão deverá conter os campos identificados."
    - "O campo observação deverá aceitar texto com limite de 2000 caracteres."
  priority: P0

REQ-GECON-003:
  pattern: event-driven
  text: "Quando o fluxo de inclusão for carregado, o sistema deverá carregar as listas de unidade de lavratura, unidade gestora e unidade de lotação e deverá limpar nome, telefone e observação."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/js/NumeroContrato.js#L14-L24
  original: "Ao iniciar a geração, disponibilizar as unidades e apresentar a interface em estado inicial."
  acceptance:
    - "As três listas deverão ser carregadas."
    - "Nome, DDD, telefone e observação deverão estar vazios após o carregamento."
  priority: P1

REQ-GECON-004:
  pattern: state-driven
  text: "Enquanto o usuário pertencer a uma unidade cujo escopo resulte em uma única opção de lavratura, o sistema deverá selecionar a unidade do usuário e bloquear a edição do campo."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/js/NumeroContrato.js#L34-L42
  original: "Para uma unidade regional ou administração hidroviária, listar somente a própria unidade e bloquear o campo quando houver uma opção."
  acceptance:
    - "Quando a lista tiver duas opções no DOM, incluindo a opção inicial, a unidade do usuário deverá ser selecionada."
    - "Após a seleção automática, o campo deverá impedir edição."
  priority: P0

REQ-GECON-005:
  pattern: event-driven
  text: "Quando o usuário confirmar a inclusão, o sistema deverá submeter o formulário ao fluxo de inclusão."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/gera_numero_contrato1.jsp#L144-L152
  original: "Ao acionar Gerar, validar e incluir o número de contrato/convênio."
  acceptance:
    - "O botão Gerar deverá submeter o formulário com a ação `incluir`."
    - "O botão Limpar deverá restaurar os valores iniciais do formulário."
  priority: P0

REQ-GECON-006:
  pattern: event-driven
  text: "Quando uma inclusão automática for processada, o sistema deverá atribuir ao número a situação Não utilizado no SIAC e o ano corrente."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L70-L83
  original: "Imediatamente após a geração, a situação deve ser Não utilizado no SIAC e o ano deve ser o ano corrente."
  acceptance:
    - "O objeto processado deverá receber a situação correspondente a `NAO_UTILIZADO`."
    - "O ano deverá ser obtido do calendário no momento do processamento."
  priority: P0

REQ-GECON-007:
  pattern: event-driven
  text: "Quando a inclusão automática for concluída, o sistema deverá exibir os números formatados nos padrões SIAC e SIASG, além da data e da hora da operação."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L90-L99
  original: "O formato deverá ser NNNNN/YYYY no SIAC e XXXXXX NNNNN/YYYY no SIASG."
  acceptance:
    - "A resposta deverá conter o número formatado para SIAC."
    - "A resposta deverá conter o número formatado para SIASG, a data e a hora da operação."
  priority: P0

REQ-GECON-008:
  pattern: event-driven
  text: "Quando o usuário selecionar um número no fluxo de alteração, o sistema deverá carregar os dados persistidos do contrato e selecionar seus valores de unidade, solicitante e situação."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/js/NumeroContrato.js#L104-L139
  original: "No fluxo de alteração, exibir os valores pré-existentes."
  acceptance:
    - "Nome, DDD, telefone e observação deverão ser preenchidos com os valores recuperados."
    - "Unidade de lavratura, unidade gestora e unidade de lotação deverão ser selecionadas com os valores recuperados."
    - "A situação persistida deverá ser marcada."
  priority: P0

REQ-GECON-009:
  pattern: ubiquitous
  text: "O sistema deverá permitir a alteração de um número selecionado, de sua unidade gestora, unidade de lotação, nome, telefone, observação e situação entre Não utilizado no SIAC e Número de Contrato/Convênio Cancelado."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/alteracao_numero_contrato1.jsp#L42-L158
  original: "Alterar dados de número de contrato/convênio, preservando os campos não editáveis definidos pelo fluxo."
  acceptance:
    - "A tela deverá oferecer os estados Não utilizado no SIAC e Número de Contrato/Convênio Cancelado."
    - "O botão Alterar deverá submeter o formulário com a ação `alterar`."
  priority: P0

REQ-GECON-010:
  pattern: state-driven
  text: "Enquanto uma alteração estiver sendo processada, o sistema deverá preservar o identificador e o ano do número selecionado."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L59-L70
  original: "Na alteração, o número do contrato e o ano não devem ser editáveis."
  acceptance:
    - "O identificador deverá ser recuperado do número armazenado na sessão."
    - "O ano deverá ser recuperado do número armazenado na sessão."
  priority: P0

REQ-GECON-011:
  pattern: event-driven
  text: "Quando o usuário solicitar uma consulta, o sistema deverá disponibilizar como filtros o número SIAC, o número SIASG, a unidade de lavratura, a unidade gestora, a unidade solicitante, o ano e a situação."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/consultar_numcontrato1.jsp#L49-L129
  original: "O filtro da consulta deve ser composto de pelo menos uma informação da RN2.4.1."
  acceptance:
    - "A tela deverá apresentar os sete filtros identificados."
    - "A tela deverá instruir o usuário a selecionar pelo menos um campo."
  priority: P0

REQ-GECON-012:
  pattern: event-driven
  text: "Quando o usuário consultar com um número SIAC preenchido com comprimento diferente de 10 caracteres, o sistema deverá informar que o formato NNNNN/YYYY é inválido."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/js/NumeroContrato.js#L52-L73
  original: "O número do contrato/convênio no SIAC deve obedecer à máscara NNNNN/YYYY."
  acceptance:
    - "Um valor preenchido com comprimento diferente de 10 deverá gerar mensagem de formato inválido."
    - "Um valor vazio não deverá gerar esta mensagem específica."
  priority: P0

REQ-GECON-013:
  pattern: event-driven
  text: "Quando o usuário consultar com um número SIASG preenchido com comprimento diferente de 17 caracteres, o sistema deverá informar que o formato XXXXXX NNNNN/YYYY é inválido."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/js/NumeroContrato.js#L76-L94
  original: "O número do contrato/convênio no SIASG deve obedecer à máscara XXXXXX NNNNN/YYYY."
  acceptance:
    - "Um valor preenchido com comprimento diferente de 17 deverá gerar mensagem de formato inválido."
    - "Um valor vazio não deverá gerar esta mensagem específica."
  priority: P0

REQ-GECON-014:
  pattern: unwanted
  text: "Se o usuário enviar a consulta sem preencher os filtros obrigatórios definidos pelo formulário, então o sistema deverá impedir o envio e informar o campo ou filtro inválido."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/consultar_numcontrato1.jsp#L42-L48
  original: "Caso nenhum campo da consulta seja informado, exibir MS12."
  notes: "NEEDS-CLARIFICATION: o código de validação do servidor que impõe pelo menos um filtro e o texto exato de MS12 não estão identificados nas fontes consultadas."
  acceptance:
    - "A consulta sem qualquer filtro deverá ser rejeitada."
    - "A mensagem exibida deverá ser identificada como MS12."
  priority: P0

REQ-GECON-015:
  pattern: event-driven
  text: "Quando o usuário executar uma consulta válida, o sistema deverá filtrar os números de contrato/convênio pelos valores enviados no formulário."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L105-L113
  original: "Consultar número de contrato/convênio segundo os filtros informados."
  acceptance:
    - "O sistema deverá converter os filtros do formulário em um objeto de filtro."
    - "O sistema deverá encaminhar o objeto para a operação de filtragem."
  priority: P0

REQ-GECON-016:
  pattern: unwanted
  text: "Se uma consulta válida não retornar números de contrato/convênio, então o sistema deverá exibir a mensagem de não encontrado e encaminhar o usuário ao fluxo de falha da consulta."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L109-L119
  original: "Caso o número informado não exista no sistema, exibir MS4."
  acceptance:
    - "Uma lista vazia deverá encaminhar para `fracassoConsulta`."
    - "A mensagem deverá ser obtida como `persistencia.naoEncontrado` para Número de Contrato."
  priority: P0

REQ-GECON-017:
  pattern: event-driven
  text: "Quando uma consulta retornar resultados, o sistema deverá armazenar a lista na sessão e encaminhar o usuário ao fluxo de resultados."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L109-L113
  original: "O resultado da consulta deve apresentar os dados do número, unidades, solicitante, situação e observação."
  acceptance:
    - "Uma lista não vazia deverá ser armazenada no atributo de sessão `listaNumeros`."
    - "O fluxo deverá encaminhar para `sucessoConsulta`."
  priority: P0

REQ-GECON-018:
  pattern: event-driven
  text: "Quando o usuário detalhar um resultado da consulta, o sistema deverá carregar o registro correspondente e apresentar os números SIAC e SIASG, as unidades, o solicitante, o telefone, a situação e a observação."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L123-L153
  original: "O resultado detalhado deve exibir os campos definidos na RN2.4.3."
  acceptance:
    - "O registro detalhado deverá ser localizado pelo identificador selecionado na lista da sessão."
    - "A representação do telefone deverá combinar DDD e número, inserindo separador após quatro dígitos quando aplicável."
  priority: P1

REQ-GECON-019:
  pattern: event-driven
  text: "Quando o usuário iniciar a inclusão manual, o sistema deverá aceitar o número e o ano informados como inclusão manual e encaminhar a requisição à mesma ação de processamento."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/xml/AcaoNumeroContrato.xml#L85-L100
  original: "Cadastrar número de contrato/convênio não gerado pelo GECON."
  acceptance:
    - "A configuração deverá mapear `gera_numero_contrato_antigo` para o tipo de ação de número de contrato."
    - "O processamento deverá identificar a operação como inclusão manual."
  priority: P0

REQ-GECON-020:
  pattern: event-driven
  text: "Quando uma inclusão manual for processada, o sistema deverá atribuir ao objeto o ano e o número de contrato/convênio informados pelo usuário."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L164-L178
  original: "Na inclusão manual, inserir o número do contrato e o ano informados."
  acceptance:
    - "O ano informado deverá ser convertido para inteiro e atribuído ao objeto."
    - "O número informado deverá ser convertido para inteiro e atribuído ao objeto."
    - "O objeto deverá ser marcado como inclusão manual."
  priority: P0

REQ-GECON-021:
  pattern: unwanted
  text: "Se o número de contrato/convênio manual já estiver cadastrado para o mesmo ano e unidade gestora, então o sistema deverá rejeitar o cadastro."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L164-L190
  original: "O número não pode estar cadastrado no banco para o mesmo ano e Unidade Gestora."
  notes: "NEEDS-CLARIFICATION: a verificação de unicidade não aparece no trecho da ação; identificar a regra implementada em FachadaGerarNumeroContrato ou no repositório persistente."
  acceptance:
    - "Um cadastro duplicado deverá ser rejeitado antes da confirmação de sucesso."
    - "A mensagem e o fluxo de falha para duplicidade deverão ser definidos."
  priority: P0

REQ-GECON-022:
  pattern: unwanted
  text: "Se o número manual não atender à regra de precedência em relação aos números cadastrados para o ano corrente, então o sistema deverá rejeitar o cadastro."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L164-L190
  original: "O número do contrato tem que ser menor que o último número cadastrado para o ano corrente."
  notes: "NEEDS-CLARIFICATION: a comparação com o último número cadastrado não está visível na ação; identificar a implementação da fachada ou da persistência."
  acceptance:
    - "Um número que viole a precedência deverá ser rejeitado."
    - "A definição de 'último número' e a mensagem de rejeição deverão ser documentadas."
  priority: P1

REQ-GECON-023:
  pattern: unwanted
  text: "Se a sessão não contiver o número selecionado durante uma alteração ou detalhamento, então o sistema deverá interromper a operação e informar falha de sistema."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L62-L67
  original: "Se a sessão expirar, informar falha e solicitar o reinício da operação."
  acceptance:
    - "A alteração sem número na sessão deverá lançar falha de sistema."
    - "A mensagem deverá solicitar que o usuário reinicie a operação."
  priority: P1

REQ-GECON-024:
  pattern: unwanted
  text: "Se o identificador detalhado não pertencer à lista de resultados armazenada na sessão, então o sistema deverá rejeitar a operação por erro de segurança."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L129-L153
  original: "O detalhamento deve permitir somente registros pertencentes ao resultado da consulta atual."
  acceptance:
    - "Um identificador que não seja encontrado na lista da sessão deverá gerar erro de segurança."
    - "Nenhum registro fora da lista da sessão deverá ser carregado para detalhamento."
  priority: P0

REQ-GECON-025:
  pattern: ubiquitous
  text: "O sistema deverá permitir cancelar o fluxo atual, limpar os dados do formulário e retornar ao menu do GECON."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/consultar_numcontrato1.jsp#L134-L138
  original: "A qualquer momento, cancelar a operação, limpar entradas e retornar ao fluxo chamador."
  acceptance:
    - "A tela de consulta deverá oferecer os comandos Consultar, Limpar e Cancelar."
    - "O comando Cancelar deverá retornar ao menu do GECON."
  priority: P1

REQ-GECON-026:
  pattern: ubiquitous
  text: "O sistema deverá disponibilizar uma ação de impressão da interface em cada fluxo que exigir impressão."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/gera_numero_contrato1.jsp#L39-L46
  original: "A qualquer momento, o sistema deve oferecer a possibilidade de imprimir a interface."
  notes: "NEEDS-CLARIFICATION: não foi localizado um controle de impressão no JSP consultado; confirmar se a regra permanece aplicável e em quais telas."
  acceptance:
    - "Cada fluxo aplicável deverá apresentar um comando de impressão identificável."
    - "A lista de fluxos aplicáveis deverá ser definida antes da implementação."
  priority: P2
