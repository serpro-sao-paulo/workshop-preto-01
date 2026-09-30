# CDU-Gerar Número de Contrato

Conteúdo do Artefato

|                          |
|--------------------------|
| **Histórico de Versões** |

<table style="width:94%;">
<colgroup>
<col style="width: 15%" />
<col style="width: 9%" />
<col style="width: 26%" />
<col style="width: 13%" />
<col style="width: 14%" />
<col style="width: 14%" />
</colgroup>
<tbody>
<tr>
<td><strong>Data</strong></td>
<td><strong>Versão</strong></td>
<td><strong>Descrição</strong></td>
<td><strong>Autor</strong></td>
<td><strong>Revisor</strong></td>
<td><strong>Aprovado por</strong></td>
</tr>
<tr>
<td>13/03/2006</td>
<td>1.0</td>
<td>Elaboração do documento.</td>
<td>Célia Lima</td>
<td>Leonardo Jardim</td>
<td></td>
</tr>
<tr>
<td>03/04/2006</td>
<td>1.1</td>
<td>Alteração da formatação do documento.</td>
<td>Célia Lima</td>
<td>Leonardo Jardim</td>
<td></td>
</tr>
<tr>
<td>17/04/2006</td>
<td>1.2</td>
<td>Definição dos critérios de aceite.</td>
<td>Célia Lima</td>
<td>Leonardo Jardim</td>
<td></td>
</tr>
<tr>
<td>25/04/2005</td>
<td>1.3</td>
<td>Adequação às novas definições de requisitos.</td>
<td>Célia Lima</td>
<td>Leonardo Jardim</td>
<td></td>
</tr>
<tr>
<td>27/04/2005</td>
<td>1.4</td>
<td>Alteração do fluxo principal e dos critérios de aceite.</td>
<td>Leonardo Jardim e Célia Lima</td>
<td>Fernando Paiva</td>
<td></td>
</tr>
<tr>
<td>30/04/2006</td>
<td>1.5</td>
<td>Alteração do fluxo principal e alteração do tópico 9.</td>
<td>Célia Lima</td>
<td>Leonardo Jardim</td>
<td></td>
</tr>
<tr>
<td>02/05/2006</td>
<td>1.6</td>
<td>Inclusão do fluxo Consultar Número de Contrato/Convênio.</td>
<td>Célia Lima</td>
<td>Leonardo Jardim</td>
<td></td>
</tr>
<tr>
<td>15/05/2006</td>
<td>1.7</td>
<td>Alteração das chamadas dos fluxos de exceção</td>
<td>Célia Lima<br />
Fernanda Galvão</td>
<td>Leonardo Jardim</td>
<td></td>
</tr>
<tr>
<td>21/12/2006</td>
<td>1.8</td>
<td>Alteração do ator e dos fluxo P1, A1 e A2.</td>
<td>Célia Lima</td>
<td>Leonardo Jardim</td>
<td></td>
</tr>
<tr>
<td>18/01/2007</td>
<td>1.9</td>
<td>Correção das exceções no passo P1.5 do fluxo principal.</td>
<td>Leonardo Jardim</td>
<td>Wescley Guimarães</td>
<td></td>
</tr>
<tr>
<td>06/11/2007</td>
<td>1.10</td>
<td>Reestruturação do artefato em função da reelaboração das regras de negócio.</td>
<td>Célia Lima</td>
<td></td>
<td></td>
</tr>
<tr>
<td>02/06/2009</td>
<td>1.11</td>
<td>Atualização das Matrizes de Requisitos referente ao atendimento à SM 167022 sobre documentação do sistema.</td>
<td>Endrigo G. Ferreira</td>
<td></td>
<td></td>
</tr>
<tr>
<td>30/07/2009</td>
<td>1.12</td>
<td>Atualização das Matrizes de Requisitos referente ao atendimento à SM 181511 sobre documentação do sistema.</td>
<td>Endrigo G. Ferreira</td>
<td></td>
<td></td>
</tr>
<tr>
<td>23/09/2009</td>
<td>1.13</td>
<td>Inclusão do fluxo alternativo A3.</td>
<td>Endrigo G. Ferreira</td>
<td></td>
<td></td>
</tr>
</tbody>
</table>

|                                  |
|----------------------------------|
| **Especificação de Caso de Uso** |

 \
**1. Nome do Caso de Uso**\
Gerar Número de Contrato\
 \
**2. Objetivo**\
Este caso de uso tem por objetivo propiciar a geração automática de números de contrato ou convênio no DNIT. Os seguintes cenários são contemplados: Gerar Número de Contrato/Convênio, Alterar Dados de Número de Contrato/Convênio, Consultar Número de Contrato/Convênio.\
\
**3. Tipo de Caso de Uso**\
Concreto\
 \
**4. Atores**

<table style="width:94%;">
<colgroup>
<col style="width: 49%" />
<col style="width: 19%" />
<col style="width: 25%" />
</colgroup>
<tbody>
<tr>
<td rowspan="2"><strong>Nome Ator</strong></td>
<td colspan="2"><strong>Tipo</strong></td>
</tr>
<tr>
<td><strong>Primário</strong></td>
<td><strong>Secundário</strong></td>
</tr>
<tr>
<td>Usuário do Sistema</td>
<td>X</td>
<td></td>
</tr>
<tr>
<td>Gerador de Número de Contrato</td>
<td>X</td>
<td></td>
</tr>
</tbody>
</table>

 \
**5. Pré-condições**

- O sistema deve estar disponível;

- O ator deve estar cadastrado no sistema;

- O ator deve autenticar-se com sucesso, no sistema, através da inclusão do caso de uso "*[[Efetuar Login]{.underline}](https://alm.serpro/rm/resources/_Gmy5EdU2EeO1joPNNloZfQ)"*.

- Somente a opção Consultar Número de Contrato/Convênio poderá ser visualizada, e acessada, pelo ator Usuário do Sistema. 

 \
**6. Fluxo Principal**\
**P1. Gerar Número de Contrato/Convênio**\
O fluxo principal é a geração do número de contrato/convênio, incluindo-o na base de dados do sistema. \
**P1.1** **Iniciar caso de uso**\
O ator aciona a opção Gerar Número de Contrato/Convênio. **\[A1\]\[A2\]**\
**P1.2Fornecer informações para geração do número de contrato**\
O ator fornece as informações, constantes na regra **RN2.2.1**, para geração do número de contrato. \
**P1.3 Confirmar geração de número de contrato/convênio**\
O ator confirma o desejo de gerar número de contrato/convênio. \
**P1.4 Validar informações do número de contrato/convênio**\
O sistema valida as informações fornecidas, conforme a regra **RN2.2.1**.**\[E1\]\[E4\]**\
**P1.5 Gravar o número de contrato gerado**\
O sistema grava o número de contrato gerado, conforme a regra **RN2.2.2**. **\[E2\]\[E3\]**\
**P1.6Exibir o número de contrato gerado**\
O sistema exibe o número de contrato gerado, conforme as regras **RN2.2.2**,** RN2.2.3** e **RN2.2.4**.\
**P1.7 Finalizar caso de uso**\
O sistema exibe a mensagem [**[MS1]{.underline}**](https://alm.serpro/rm/resources/_FYvXJdbOEeOnQszXG_IkRw) e finaliza o caso de uso.\
 \
**7. Fluxos Alternativos**\
**A1. Alterar Dados de Número de Contrato/Convênio**\
**A1.1 Iniciar fluxo alternativo**\
O ator aciona a opção Alterar Dados de Número de Contrato/Convênio.\
**A1.2 Fornecer informações passíveis de alteração**\
O ator fornece as informações passíveis de alteração, conforme a regra **RN2.3.1**.\
**A1.3 Validar informações **\
O sistema valida as informações fornecidas pelo o ator, conforme a regra **RN2.3.1**.**\[E1\]\[E4\]**\
**A1.4 Gravar informações alteradas do número do contrato/convênio**\
O sistema grava as alterações. **\[E2\]\[E3\]**\
**A1.5 Finalizar fluxo alternativo**\
O sistema exibe a mensagem [**[MS1]{.underline}**](https://alm.serpro/rm/resources/_FYvXJdbOEeOnQszXG_IkRw) e finaliza o fluxo alternativo**.  **\
 \
**A2. Consultar Número de Contrato/Convênio**\
**A2.1 Iniciar fluxo alternativo**\
O ator aciona a opção Consultar Número de Contrato/Convênio.\
**A2.2 Informar filtro da consulta**\
O ator informa e submete o filtro da consulta, conforme as regras **RN2.4.1** e **RN2.4.2**. \
**A2.3 Validar filtro da consulta**\
O sistema valida o filtro da consulta, conforme as regras **RN2.4.1** e **RN2.4.2**.** \[E1\]\[E4\]\[E5\]**\
**A2.4 Exibir resultado da consulta**\
O sistema exibe o resultado da consulta, conforme a regra **RN2.4.3**. **\[E2\]\[E3\]**\
**A2.5 Finalizar fluxo alternativo**\
O sistema finaliza o fluxo alternativo.\
 \
**A3. Cadastrar Número de Contrato/Convênio não Gerado**\
**A3.1 Iniciar fluxo alternativo**\
O ator aciona a opção Cadastrar Número de Contrato/Convênio não Gerado\
**A3.2 Fornecer informações para geração do número de contrato**\
O ator fornece as informações, constantes na regra RN2.5.1, para geração do número de contrato. \
**A3.3 Confirmar cadastro de número de contrato/convênio**\
O ator confirma o desejo de cadastrar o número de contrato/convênio informado. \
**A3.4 Validar informações do número de contrato/convênio**\
O sistema valida as informações fornecidas, conforme a regra RN2.5.1.\[E1\]\[E4\]\
**A3.5 Gravar o número de contrato gerado**\
O sistema grava o número de contrato gerado, conforme a regra RN2.5.2 e RN2.5.3  \[E2\]\[E3\]\
**A3.6 Exibir o número de contrato gerado**\
O sistema exibe o número de contrato gerado, conforme as regras RN2.2.2.\
**A3.7 Finalizar fluxo alternativo**\
O sistema finaliza o fluxo alternativo.\
 \
**8. Fluxos de Exceção**\
**E1. Campo de preenchimento obrigatório**\
**E1.1. Iniciar fluxo de exceção**\
O sistema verifica que informações obrigatórias não foram inseridas.\
**E1.2. Notificar obrigatoriedade de preenchimento**\
O sistema notifica o ator, através da mensagem [**[MS2]{.underline}**](https://alm.serpro/rm/resources/_LIiykdbOEeOnQszXG_IkRw)ou** [[MS12]{.underline}](https://alm.serpro/rm/resources/_7j8m0dbOEeOnQszXG_IkRw) **(caso seja fluxo de consulta)**, **informando que o campo obrigatório deverá ser preenchido.\
**E1.3. Finalizar fluxo de exceção**\
O sistema finaliza o fluxo de exceção e retorna ao passo desviado.\
 \
**E2. Sistema não disponível**\
**E2.1. Iniciar fluxo de exceção**\
O sistema detecta alguma indisponibilidade que impossibilita a realização da operação.\
**E2.2. Notificar indisponibilidade do sistema**\
O sistema notifica o ator, através da mensagem [**[MS8]{.underline}**](https://alm.serpro/rm/resources/_us2YkdbOEeOnQszXG_IkRw), informando que o sistema não está disponível.\
**E2.3. Finalizar fluxo de exceção**\
O sistema finaliza o fluxo de exceção e o caso de uso.\
 \
**E3. Operação não realizada**\
**E3.1. Iniciar fluxo de exceção**\
O sistema detecta que, por motivos não previstos, a operação não pôde ser realizada.\
**E3.2. Notificar não realização de operação**\
O sistema notifica o ator, através da mensagem [**[MS9]{.underline}**](https://alm.serpro/rm/resources/_w8nbwdbOEeOnQszXG_IkRw), informando que a operação não foi realizada.\
**E3.3. Finalizar fluxo de exceção**\
O sistema finaliza o fluxo de exceção e o caso de uso.\
 \
**E4. Formato de campo inválido**\
**E4.1. Iniciar fluxo de exceção**\
O sistema verifica que o formato das informações inseridas é inválido.\
**E4.2. Notificar formato inválido**\
O sistema notifica o ator, através da mensagem [**[MS3]{.underline}**](https://alm.serpro/rm/resources/_iLkpUdbOEeOnQszXG_IkRw), do formato incorreto de preenchimento do campo.\
**E4.3. Finalizar fluxo de exceção**\
O sistema finaliza o fluxo de exceção e retorna ao passo desviado.\
 \
**E5. Número de Contrato/Convênio não encontrado**\
**E5.1. Verificar existência de número de contrato/convênio no sistema**\
O sistema verifica que o número de contrato/convênio não está cadastrado.\
**E5.2. Notificar inexistência**\
O sistema notifica o ator, através da mensagem [**[MS4]{.underline}**](https://alm.serpro/rm/resources/_k_FSwdbOEeOnQszXG_IkRw), informando que o número de contrato/convênio não foi encontrado.\
**E5.3. Finalizar fluxo de exceção**\
O sistema finaliza o fluxo de exceção e retorna ao passo desviado.\
 \
 \
**9. Pós-condições**\
**9.1. **Informação incluída deve estar armazenada em meio persistente.\
**9.2. **Deve estar armazenado em meio persistente o CPF do ator, o tipo da operação e a data e hora da execução da funcionalidade.\
 \
 \
**10. Requisitos Não Funcionais**\
Nenhum requisito não funcional identificado.\
 \
**11. Ponto de Extensão**\
Nenhum ponto de extensão identificado.\
 \
**12. Critérios de Aceite**\
**CA1. **Iniciar fluxo principal e verificar se o sistema inclui, e exibe, um número de contrato/convênio com sucesso.\
**CA2.** Iniciar o fluxo principal, seguir pelo fluxo alternativo **A1**, e verificar se o sistema altera dados do número de contrato/convênio com sucesso.\
**CA3.** Iniciar o fluxo principal, seguir pelo fluxo alternativo **A2**, e verificar se o sistema consulta, e detalha, um número de contrato/convênio com sucesso.\
**CA4.** Tentar gerar um número de contrato/convênio inserindo informações inválidas, ou deixando de preencher informações obrigatórias, e verificar se o sistema notifica o ator.\
 \
**13. Freqüência de Utilização**\
Média.\
 \
**14. Observações**\
Nenhuma observação foi identificada.\
 \
**15. Referências**

- [**[RN-Gerar Número de Contrato/Convênio]{.underline}**](https://alm.serpro/rm/resources/_DWONQ9VDEeO1joPNNloZfQ)

- [**[EMS-SIAC]{.underline}**](https://alm.serpro/rm/resources/_sCun0dYiEeO1joPNNloZfQ)
