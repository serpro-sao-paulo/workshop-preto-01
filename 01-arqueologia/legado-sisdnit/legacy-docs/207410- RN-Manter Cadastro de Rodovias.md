# RN-Manter Cadastro de Rodovias

Conteúdo do Artefato

**Histórico de Versões**\
 

|  |  |  |  |  |  |
|----|----|----|----|----|----|
| **Data** | **Versão** | **Descrição** | **Autor** | **Revisor** | **Aprovado por** |
| 07/03/2006 | 1.0 | Elaboração do Documento. | Tiago Medeiros | Leonardo Jardim |  |
| 21/06/2006 | 1.1 | Alteração do padrão do documento e das regras | Fernanda Galvão | Leonardo Jardim | Ricardo Miranda |
| 18/10/2006 | 1.2 | Alteração do campo UF nos diversos fluxos | Célia Lima | Leonardo Jardim |  |
| 20/08/2007 | 1.3 | Campo Sigla (RN2.2.1) - Inclusão da regra 3 e retirada da observação. | Célia Lima | Leonardo Jardim |  |
| 01/11/2007 | 1.4 | Atendimento à ocorrência 0002 da revisão 070206 | André Ramos |  |  |
| 02/06/2009 | 1.5 | Atualização das Matrizes de Requisitos referente ao atendimento à SM 167022 sobre documentação do sistema. | Endrigo G. Ferreira |  |  |
| 31/07/2009 | 1.6 | Atualização das Matrizes de Requisitos referente ao atendimento à SM 181511 sobre documentação do sistema. | Endrigo G. Ferreira |  |  |
| 27/12/2010 | 2.0 | Atualização da versão do artefato | Fabricio Nonato |  |  |

 \
 \
**Especificação de Regras de Negócio**\
 \
**1. Objetivo**\
O objetivo da Especificação de Regras de Negócio é documentar as regras que são aplicáveis ao negócio, e que direcionam em maior ou menor grau o funcionamento dos casos de uso. Em geral, regras de negócio constituem declarações de políticas ou condições que devem ser satisfeitas pelo processamento da aplicação.           \
 \
**2. Regras de Negócio**\
**2.1. Regras Gerais**\
**2.1.1.** A qualquer momento, o sistema deve oferecer ao usuário a possibilidade de cancelar a operação em curso e retornar ao fluxo chamador desta.\
**2.1.2**. O sistema deve oferecer ao usuário a possibilidade de limpeza das entradas de dados, fazendo com que a interface retorne ao seu estado inicial de apresentação.\
**2.1.3**. A qualquer momento, o sistema deve oferecer ao usuário a possibilidade de imprimir a interface.\
 \
**2.2. Incluir Rodovia**\
**2.2.1.** As informações a serem incluídas deverão seguir as seguintes regras:

<table style="width:86%;">
<colgroup>
<col style="width: 11%" />
<col style="width: 9%" />
<col style="width: 11%" />
<col style="width: 14%" />
<col style="width: 9%" />
<col style="width: 10%" />
<col style="width: 19%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Informação</strong></td>
<td style="text-align: center;"><strong>Editável</strong></td>
<td style="text-align: center;"><strong>Obrigatório (Sim/Não)</strong></td>
<td style="text-align: center;"><strong>Formato/ Tamanho</strong></td>
<td style="text-align: center;"><strong>Máscara</strong></td>
<td style="text-align: center;"><strong>Valores Possíveis</strong></td>
<td style="text-align: center;"><strong>Regras</strong></td>
</tr>
<tr>
<td style="text-align: center;">Sigla</td>
<td>Sim</td>
<td>Sim</td>
<td>Alfanumérico/6</td>
<td>-</td>
<td>-</td>
<td><ul>
<li><p>Caso não seja informado, apresentar mensagem <a href="https://alm.serpro/rm/resources/_LIiykdbOEeOnQszXG_IkRw"><strong><u>MS2</u></strong></a>.</p></li>
<li><p>Caso o valor informado já esteja cadastrado no sistema, apresentar a mensagem <a href="https://alm.serpro/rm/resources/_nYI00dbOEeOnQszXG_IkRw"><strong><u>MS5</u></strong></a>. Exemplo de sigla: BR-030</p></li>
<li><p>O valor informado pelo ator deve ser convertido para maiúscula.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;">Descrição</td>
<td>Sim</td>
<td>Sim</td>
<td>Alfanumérico/80</td>
<td>-</td>
<td>-</td>
<td>Caso não seja informado, apresentar mensagem <a href="https://alm.serpro/rm/resources/_LIiykdbOEeOnQszXG_IkRw"><strong><u>MS2</u></strong></a>.</td>
</tr>
<tr>
<td style="text-align: center;">UF</td>
<td>Sim</td>
<td>Sim</td>
<td>-</td>
<td>-</td>
<td>Todas as UF’s do Brasil</td>
<td><ul>
<li><p>Caso não seja informado, apresentar mensagem <a href="https://alm.serpro/rm/resources/_LIiykdbOEeOnQszXG_IkRw"><strong><u>MS2</u></strong></a>.</p></li>
<li><p>As UF’s devem ser apresentadas em ordem alfabética.</p></li>
<li><p>Mais de uma UF pode ser selecionada.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;">Situação</td>
<td>Sim</td>
<td>Sim</td>
<td>-</td>
<td>-</td>
<td>- Ativa<br />
- Inativa</td>
<td>O valor padrão da lista é “Ativa”</td>
</tr>
</tbody>
</table>

** **\
**2.3. Alterar Rodovias**\
**2.3.1.** As informações a serem alteradas deverão seguir as seguintes regras:

<table style="width:86%;">
<colgroup>
<col style="width: 12%" />
<col style="width: 9%" />
<col style="width: 12%" />
<col style="width: 15%" />
<col style="width: 9%" />
<col style="width: 10%" />
<col style="width: 16%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Informação</strong></td>
<td style="text-align: center;"><strong>Editável</strong></td>
<td style="text-align: center;"><strong>Obrigatório (Sim/Não)</strong></td>
<td style="text-align: center;"><strong>Formato/ Tamanho</strong></td>
<td style="text-align: center;"><strong>Máscara</strong></td>
<td style="text-align: center;"><strong>Valores Possíveis</strong></td>
<td style="text-align: center;"><strong>Regras</strong></td>
</tr>
<tr>
<td style="text-align: center;">Sigla</td>
<td>Não</td>
<td>Sim</td>
<td>Alfanumérico/6</td>
<td>-</td>
<td>-</td>
<td>-</td>
</tr>
<tr>
<td style="text-align: center;">Descrição</td>
<td>Sim</td>
<td>Sim</td>
<td>Alfanumérico/80</td>
<td>-</td>
<td>-</td>
<td>Caso não seja informado, apresentar mensagem <a href="https://alm.serpro/rm/resources/_LIiykdbOEeOnQszXG_IkRw"><strong><u>MS2</u></strong></a>.</td>
</tr>
<tr>
<td style="text-align: center;">UF</td>
<td>Sim</td>
<td>Sim</td>
<td>-</td>
<td>-</td>
<td>Todas as UF’s do Brasil</td>
<td><ul>
<li><p>Caso não seja informado, apresentar mensagem <a href="https://alm.serpro/rm/resources/_LIiykdbOEeOnQszXG_IkRw"><strong><u>MS2</u></strong></a>.</p></li>
<li><p>Mais de uma UF pode ser selecionada.</p></li>
<li><p>Mais de uma UF pode ser desvinculada da Sigla.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;">Situação</td>
<td>Sim</td>
<td>Sim</td>
<td>-</td>
<td>-</td>
<td>- Ativa<br />
- Inativa</td>
<td>-</td>
</tr>
</tbody>
</table>

 \
 \
**2.4. Consultar Rodovias**\
**2.4.1.** O filtro da consulta deve ser composto de pelo menos uma das seguintes informações:

<table style="width:86%;">
<colgroup>
<col style="width: 12%" />
<col style="width: 9%" />
<col style="width: 12%" />
<col style="width: 14%" />
<col style="width: 9%" />
<col style="width: 10%" />
<col style="width: 17%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Informação</strong></td>
<td style="text-align: center;"><strong>Editável</strong></td>
<td style="text-align: center;"><strong>Obrigatório</strong></td>
<td style="text-align: center;"><strong>Formato/ Tamanho</strong></td>
<td style="text-align: center;"><strong>Máscara</strong></td>
<td style="text-align: center;"><strong>Valores Possíveis</strong></td>
<td style="text-align: center;"><strong>Regras</strong></td>
</tr>
<tr>
<td style="text-align: center;">Sigla</td>
<td>Sim</td>
<td><strong>*</strong></td>
<td>Alfanumérico/6</td>
<td>-</td>
<td>-</td>
<td>-</td>
</tr>
<tr>
<td style="text-align: center;">Descrição</td>
<td>Sim</td>
<td><strong>*</strong></td>
<td>Alfanumérico/80</td>
<td>-</td>
<td>-</td>
<td>Caso o usuário deseje informar a descrição, deve ser informado pelo menos 3 caracteres para consulta, caso não seja informado, apresentar mensagem <a href="https://alm.serpro/rm/resources/_s5qHMdbPEeOnQszXG_IkRw"><strong><u>MS20</u></strong></a>.</td>
</tr>
<tr>
<td style="text-align: center;">UF</td>
<td>Sim</td>
<td><strong>*</strong></td>
<td>-</td>
<td>-</td>
<td>Todas UF’s do Brasil </td>
<td>As UF’s devem ser apresentadas em ordem alfabética.</td>
</tr>
<tr>
<td style="text-align: center;">Situação</td>
<td>Sim</td>
<td>Sim</td>
<td>-</td>
<td>-</td>
<td>- Ativa<br />
- Inativa</td>
<td>Valor padrão: Ativa</td>
</tr>
</tbody>
</table>

**\* Pelo menos uma dessas informações deve ser preenchida para consulta**\
 \
**2.4.2**. (\*)Caso não seja informado nenhum parâmetro de consulta, apresentar mensagem [**[MS12]{.underline}**](https://alm.serpro/rm/resources/_7j8m0dbOEeOnQszXG_IkRw).\
**2.4.3**. As informações a serem apresentadas no resultado da consulta são as seguintes:

|                |             |                                              |
|:--------------:|-------------|----------------------------------------------|
| **Informação** | **Máscara** | **Regras**                                   |
|     Sigla      | \-          | \-                                           |
|   Descrição    | \-          | \-                                           |
|       UF       | \-          | Mais de uma UF pode ser retornada por sigla. |
|    Situação    | \-          | \-                                           |

 \
**2.4.4**. As informações a serem apresentadas no detalhamento do resultado da consulta são as seguintes:

|                |             |                                              |
|:--------------:|-------------|----------------------------------------------|
| **Informação** | **Máscara** | **Regras**                                   |
|     Sigla      | \-          | \-                                           |
|   Descrição    | \-          | \-                                           |
|       UF       | \-          | Mais de uma UF pode ser retornada por sigla. |
|    Situação    | \-          | \-                                           |

** **
