# EARS - CDU GECON: Gerar Número de Contrato

> Conversão proposta de `CDU_Gecon.md`. O caso de uso original foi preservado.
> Requisitos com `NEEDS-CLARIFICATION` registram regras do CDU cuja evidência executável ou métrica não foi localizada.

REQ-GECON-CDU-001:
  pattern: ubiquitous
  text: "O sistema deverá disponibilizar os fluxos de gerar, alterar, consultar e cadastrar manualmente número de contrato ou convênio."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/xml/AcaoNumeroContrato.xml#L27-L100
  original: "Os cenários contemplados são gerar, alterar, consultar e cadastrar número não gerado pelo GECON."
  acceptance:
    - "A configuração deverá mapear as quatro operações para `AcaoNumeroContrato`."
    - "Cada operação deverá possuir tela de entrada e encaminhamento de resultado ou falha."
  priority: P0

REQ-GECON-CDU-002:
  pattern: event-driven
  text: "Quando o usuário confirmar a geração automática, o sistema deverá validar os dados do formulário e processar a inclusão do número de contrato ou convênio."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/xml/AcaoNumeroContrato.xml#L27-L43
  original: "O ator fornece as informações, confirma a geração e o sistema valida e grava o número."
  acceptance:
    - "A ação de inclusão deverá usar `NumeroContratoForm` com validação habilitada."
    - "Uma requisição de inclusão deverá ser encaminhada a `AcaoNumeroContrato`."
  priority: P0

REQ-GECON-CDU-003:
  pattern: event-driven
  text: "Quando a inclusão automática for processada, o sistema deverá gravar o número e encaminhar a resposta de sucesso com os valores formatados para SIAC e SIASG."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L70-L99
  original: "O sistema grava o número, exibe o número gerado e finaliza com MS1."
  acceptance:
    - "O processamento deverá chamar a operação de persistência do número."
    - "A resposta deverá disponibilizar `numeroContratoSIAC` e `numeroContratoSIASG`."
    - "A resposta deverá disponibilizar data e hora da operação."
  priority: P0

REQ-GECON-CDU-004:
  pattern: event-driven
  text: "Quando a geração automática for concluída, o sistema deverá apresentar o número SIAC, o número SIASG, a data e a hora da operação."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/gera_numero_contrato5.jsp#L17-L35
  original: "Exibir o número gerado conforme as regras de formato e finalizar o caso de uso."
  acceptance:
    - "A tela de sucesso deverá exibir o valor SIAC."
    - "A tela de sucesso deverá exibir o valor SIASG."
    - "A tela de sucesso deverá exibir data e hora."
  priority: P0

REQ-GECON-CDU-005:
  pattern: event-driven
  text: "Quando o usuário iniciar a alteração de um número de contrato ou convênio, o sistema deverá oferecer a seleção do número e os campos passíveis de alteração."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/alteracao_numero_contrato1.jsp#L42-L158
  original: "No fluxo A1, o ator seleciona um número e fornece as informações passíveis de alteração."
  acceptance:
    - "A tela deverá oferecer a seleção do número SIAC."
    - "A tela deverá oferecer unidade de lavratura, unidade gestora, unidade de lotação, nome, telefone, observação e situação."
  priority: P0

REQ-GECON-CDU-006:
  pattern: event-driven
  text: "Quando o usuário confirmar uma alteração válida, o sistema deverá preservar o identificador e o ano do registro selecionado, gravar os demais dados permitidos e encaminhar o resultado de sucesso."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L59-L99
  original: "O sistema valida, grava as alterações, exibe MS1 e finaliza o fluxo alternativo."
  acceptance:
    - "O identificador deverá ser recuperado do registro armazenado na sessão."
    - "O ano deverá permanecer igual ao do registro armazenado na sessão."
    - "A operação deverá encaminhar para `sucessoProcessamento` quando concluída."
  priority: P0

REQ-GECON-CDU-007:
  pattern: event-driven
  text: "Quando o usuário iniciar uma consulta, o sistema deverá aceitar filtros de número SIAC, número SIASG, unidades, ano e situação e encaminhar a busca."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/consultar_numcontrato1.jsp#L49-L129
  original: "No fluxo A2, o ator informa e submete o filtro conforme RN2.4.1 e RN2.4.2."
  acceptance:
    - "A tela deverá apresentar os filtros de número SIAC, número SIASG, unidade de lavratura, unidade gestora, unidade solicitante, ano e situação."
    - "O botão Consultar deverá executar a validação do formulário antes do envio."
  priority: P0

REQ-GECON-CDU-008:
  pattern: event-driven
  text: "Quando a consulta retornar uma lista não vazia, o sistema deverá armazenar a lista na sessão e apresentar a tela de resultados."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L105-L114
  original: "O sistema exibe o resultado da consulta e finaliza o fluxo alternativo."
  acceptance:
    - "A lista retornada deverá ser armazenada no atributo de sessão `listaNumeros`."
    - "O sistema deverá encaminhar para `sucessoConsulta`."
  priority: P0

REQ-GECON-CDU-009:
  pattern: event-driven
  text: "Quando a tela de resultados for apresentada, o sistema deverá listar os números SIAC e SIASG e a unidade de lavratura, permitindo selecionar um registro para detalhamento."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/consultar_numcontrato2.jsp#L20-L62
  original: "O sistema exibe o resultado da consulta conforme RN2.4.3."
  acceptance:
    - "Cada resultado deverá exibir número SIAC, número SIASG e unidade de lavratura."
    - "Cada resultado deverá conter uma ação de detalhamento com seu identificador."
  priority: P1

REQ-GECON-CDU-010:
  pattern: event-driven
  text: "Quando o usuário selecionar um resultado, o sistema deverá carregar somente o registro pertencente à lista de resultados da sessão e apresentar seus dados detalhados."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L123-L153
  original: "O sistema consulta e detalha um número de contrato ou convênio com sucesso."
  acceptance:
    - "O identificador deverá ser comparado com os registros presentes em `listaNumeros`."
    - "O registro encontrado deverá ser carregado e encaminhado para `sucessoDetalhar`."
    - "O registro detalhado deverá ser armazenado na sessão para eventual alteração."
  priority: P0

REQ-GECON-CDU-011:
  pattern: unwanted
  text: "Se o identificador selecionado não pertencer à lista de resultados da sessão, então o sistema deverá rejeitar o detalhamento com erro de segurança."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L144-L153
  original: "O detalhamento deve impedir o acesso a um registro fora do resultado da consulta atual."
  acceptance:
    - "Um identificador ausente da lista da sessão deverá lançar `ERRO_SEGURANCA`."
    - "Nenhum registro fora da lista deverá ser carregado."
  priority: P0

REQ-GECON-CDU-012:
  pattern: event-driven
  text: "Quando o usuário iniciar o cadastro manual, o sistema deverá encaminhar a entrada para o fluxo de inclusão de número antigo e aceitar o número e o ano informados."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/xml/AcaoNumeroContrato.xml#L85-L100
  original: "No fluxo A3, o ator cadastra número de contrato ou convênio não gerado pelo GECON."
  acceptance:
    - "A ação `gera_numero_contrato_antigo` deverá usar `NumeroContratoForm`."
    - "A ação deverá encaminhar sucesso para a tela de inclusão concluída."
  priority: P0

REQ-GECON-CDU-013:
  pattern: event-driven
  text: "Quando um cadastro manual for processado, o sistema deverá marcar a operação como inclusão manual e atribuir ao objeto o ano e o número fornecidos."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L164-L178
  original: "O ator fornece e confirma os dados; o sistema valida e grava o número informado conforme RN2.5."
  acceptance:
    - "O objeto deverá receber `inclusaoManual=true`."
    - "O número e o ano do formulário deverão ser convertidos e atribuídos ao objeto."
  priority: P0

REQ-GECON-CDU-014:
  pattern: unwanted
  text: "Se uma operação não puder ser realizada por motivo não previsto, então o sistema deverá encaminhar a falha de processamento ao fluxo correspondente."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L51-L58
  original: "Na operação não realizada, notificar MS9 e finalizar o caso de uso."
  notes: "NEEDS-CLARIFICATION: a mensagem MS9 e a política de tratamento para cada exceção não aparecem no trecho da ação; identificar a camada de exceção global."
  acceptance:
    - "O processamento deverá iniciar com o encaminhamento de falha como padrão."
    - "Uma falha não prevista não deverá ser apresentada como sucesso."
  priority: P0

REQ-GECON-CDU-015:
  pattern: unwanted
  text: "Se a sessão não contiver o número selecionado durante uma alteração, então o sistema deverá interromper a operação e solicitar que o usuário reinicie o processo."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L62-L67
  original: "Se a sessão expirar, notificar falha e reiniciar a operação."
  acceptance:
    - "A alteração sem objeto na sessão deverá lançar falha de sistema."
    - "A exceção deverá conter orientação para reiniciar a operação."
  priority: P1

REQ-GECON-CDU-016:
  pattern: unwanted
  text: "Se uma consulta válida não retornar registros, então o sistema deverá informar que o número de contrato não foi encontrado e encaminhar ao fluxo de falha da consulta."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L105-L119
  original: "Na exceção E5, notificar MS4 quando o número não estiver cadastrado."
  acceptance:
    - "Uma lista vazia deverá encaminhar para `fracassoConsulta`."
    - "A mensagem deverá ser obtida com a chave `persistencia.naoEncontrado`."
  priority: P0

REQ-GECON-CDU-017:
  pattern: unwanted
  text: "Se os dados obrigatórios de inclusão, alteração ou cadastro manual estiverem ausentes, então o sistema deverá rejeitar a operação e solicitar o preenchimento do campo obrigatório."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/xml/AcaoNumeroContrato.xml#L27-L100
  original: "Na exceção E1, notificar MS2; na consulta, notificar MS12."
  notes: "NEEDS-CLARIFICATION: os validadores e o texto das mensagens MS2/MS12 não estão presentes nas fontes lidas; confirmar regras por campo."
  acceptance:
    - "Cada ação deverá executar validação antes do processamento."
    - "A consulta sem filtro deverá usar a mensagem MS12, e os demais campos obrigatórios deverão usar MS2."
  priority: P0

REQ-GECON-CDU-018:
  pattern: unwanted
  text: "Se um campo tiver formato inválido, então o sistema deverá impedir o processamento e informar o formato esperado ao usuário."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/js/NumeroContrato.js#L52-L94
  original: "Na exceção E4, notificar MS3 quando o formato inserido for inválido."
  acceptance:
    - "Um número SIAC preenchido com comprimento diferente de 10 deverá ser rejeitado."
    - "Um número SIASG preenchido com comprimento diferente de 17 deverá ser rejeitado."
    - "A mensagem deverá indicar a máscara correspondente ao campo inválido."
  priority: P0

REQ-GECON-CDU-019:
  pattern: state-driven
  text: "Enquanto uma inclusão automática estiver sendo processada, o sistema deverá atribuir a situação Não utilizado no SIAC e o ano corrente ao número."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L74-L83
  original: "Após a geração, o número deve iniciar como Não utilizado no SIAC e receber o ano corrente."
  acceptance:
    - "A situação inicial deverá ser o valor de `NAO_UTILIZADO`."
    - "O ano deverá ser obtido do calendário no instante do processamento."
  priority: P0

REQ-GECON-CDU-020:
  pattern: unwanted
  text: "Se o sistema estiver indisponível durante uma operação, então o sistema deverá informar a indisponibilidade e finalizar o fluxo sem confirmar a operação."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/xml/AcaoNumeroContrato.xml#L27-L100
  original: "Na exceção E2, notificar MS8 quando a indisponibilidade impedir a operação."
  notes: "NEEDS-CLARIFICATION: não foi localizada uma implementação de detecção de indisponibilidade ou da mensagem MS8 nas fontes consultadas."
  acceptance:
    - "Uma indisponibilidade deverá impedir o encaminhamento de sucesso."
    - "A mensagem deverá ser identificada como MS8."
  priority: P1

REQ-GECON-CDU-021:
  pattern: state-driven
  text: "Enquanto o caso de uso estiver concluído com sucesso, o sistema deverá disponibilizar o resultado persistido da operação ao usuário."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L90-L99
  original: "A informação incluída deve estar armazenada em meio persistente."
  notes: "NEEDS-CLARIFICATION: a persistência concreta e a confirmação transacional são delegadas à fachada, não evidenciadas neste arquivo."
  acceptance:
    - "O fluxo de sucesso deverá ser alcançado somente após o processamento retornar o objeto."
    - "O número exibido deverá corresponder ao objeto processado."
  priority: P0

REQ-GECON-CDU-022:
  pattern: ubiquitous
  text: "O sistema deverá registrar o CPF do ator, o tipo da operação e a data e hora de execução da funcionalidade."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L90-L99
  original: "Deve estar armazenado em meio persistente o CPF do ator, o tipo da operação e a data e hora da execução."
  notes: "NEEDS-CLARIFICATION: o trecho da ação só evidencia data e hora para exibição; não evidencia a persistência do CPF, tipo da operação ou trilha de auditoria."
  acceptance:
    - "A operação deverá registrar o tipo de operação executado."
    - "A operação deverá registrar data e hora."
    - "A origem e o armazenamento do CPF do ator deverão ser definidos."
  priority: P1

REQ-GECON-CDU-023:
  pattern: ubiquitous
  text: "O sistema deverá permitir ao usuário iniciar nova consulta ou retornar ao menu após a apresentação dos resultados."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/jsp/consultar_numcontrato2.jsp#L57-L62
  original: "Após consultar, o ator deve poder iniciar nova consulta ou retornar ao menu."
  acceptance:
    - "A tela de resultados deverá oferecer o comando Nova Consulta."
    - "A tela de resultados deverá oferecer o comando Retornar ao Menu."
  priority: P1

REQ-GECON-CDU-024:
  pattern: unwanted
  text: "Se o ator solicitar um detalhamento fora dos resultados armazenados na sessão, então o sistema deverá negar o acesso por erro de segurança."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L129-L153
  original: "O sistema deve proteger o detalhamento contra registros não pertencentes à consulta atual."
  acceptance:
    - "O registro somente deverá ser carregado após correspondência do identificador na lista da sessão."
    - "A ausência de correspondência deverá produzir erro de segurança."
  priority: P0

REQ-GECON-CDU-025:
  pattern: ubiquitous
  text: "O sistema deverá permitir que somente usuários autorizados acessem os fluxos de geração, alteração e cadastro manual, enquanto o fluxo de consulta poderá ser acessado por usuários do sistema autorizados."
  source_legacy: 01-arqueologia/legado-sisdnit/programs/Gecon/java/AcaoNumeroContrato.java#L45-L58
  original: "O ator deve autenticar-se; somente a consulta pode ser visualizada e acessada pelo Usuário do Sistema."
  notes: "NEEDS-CLARIFICATION: os controles de autenticação e autorização não estão implementados na ação apresentada; indicar o módulo responsável e as permissões por fluxo."
  acceptance:
    - "Um usuário sem permissão de geração não deverá acessar o fluxo de inclusão."
    - "Um usuário sem permissão de alteração não deverá alterar registros."
    - "As permissões exigidas para consulta deverão ser explicitamente configuradas."
  priority: P0
