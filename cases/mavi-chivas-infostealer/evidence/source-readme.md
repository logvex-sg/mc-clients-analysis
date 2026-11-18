# Relatório Técnico de Análise de Malware
## Infostealer disfarçado de mod para Minecraft — `mavi-chivas.exe`

Análise comportamental via sandbox de uma amostra distribuída em servidor Discord como modificação para o jogo Minecraft.

|                     |                                                              |
| :------------------ | :----------------------------------------------------------- |
| **Analista**        | [@guisosi](https://github.com/guisosi)                       |
| **Data da análise** | 2026-05-11 03:15 UTC                                         |
| **Revisão**         | 2026-11-15, a partir do relatório público da sandbox         |
| **Classificação**   | Malicioso — **Infostealer (Credential Stealer)**, confirmado |
| **Vetor**           | Engenharia social — isca de mod de jogo, via Discord         |
| **Ferramentas**     | ANY.RUN (sandbox dinâmico), Microsoft Defender (triagem)     |
| **Relatório**       | ANY.RUN, tarefa pública (busque pelo SHA256 da seção 2)      |
| **Tipo**            | Análise pessoal / educacional — pesquisa independente        |

> [!WARNING]
> **Nenhuma amostra executável é hospedada neste repositório.** Todos os IOCs de rede estão defangados (`hxxps`, `[.]com`) conforme convenção de documentação de segurança — não desfange nem acesse os endereços listados.

> [!NOTE]
> Investigação **retrospectiva** de uma das minhas primeiras incursões práticas em cibersegurança. O processo original não seguiu metodologia formal estruturada desde o início; os achados foram reorganizados posteriormente. As regras de detecção da seção 11 foram escritas depois, a partir do comportamento documentado.

> [!IMPORTANT]
> **Revisão de 2026-11-15.** Reli a execução no relatório público da sandbox e decodifiquei o PowerShell em Base64. Três conclusões da primeira versão estavam erradas e foram corrigidas: o `csc.exe` não compila o payload, e sim um helper que esconde a janela (seção 4.4); não há segundo estágio, o arquivo do Dropbox é a própria amostra (seção 4.2); e a conexão com o Discord agora tem evidência de rede (seção 4.7). A regra Sigma 04 também foi corrigida, porque não casava com o comando real da amostra.

---

## Índice

1. [Resumo](#1-resumo)
2. [Informações da Amostra](#2-informações-da-amostra)
3. [Metodologia](#3-metodologia)
4. [Achados — Análise Comportamental](#4-achados--análise-comportamental)
5. [Cadeia de Execução](#5-cadeia-de-execução)
6. [Mapeamento MITRE ATT&CK](#6-mapeamento-mitre-attck)
7. [Indicadores de Comprometimento](#7-indicadores-de-comprometimento-iocs)
8. [Classificação de Risco](#8-classificação-de-risco)
9. [Lições Aprendidas](#9-lições-aprendidas)
10. [Conclusão](#10-conclusão)
11. [Regras de Detecção — Sigma](#11-regras-de-detecção--sigma)

---

## 1. Resumo

Documento a análise comportamental de uma amostra distribuída em servidor Discord como um "mod" para o jogo Minecraft, que identifiquei como maliciosa.

A análise inicial indicava comportamento de **dropper/downloader**. A revisão dos artefatos e **arquivos efetivamente tocados** durante a execução confirmou que a amostra é, na verdade, um **infostealer** — com furto confirmado de dados de credencial, cookie de sessão e histórico do navegador, seguido de conexão com o Discord.

Conduzi a análise em ambiente de sandbox (ANY.RUN), complementada por triagem de detecção com o Microsoft Defender e revisão dos indicadores de comprometimento exportados pela própria plataforma.

---

## 2. Informações da Amostra

| Campo | Valor |
|:--|:--|
| Vetor de distribuição | Arquivo apresentado como mod para Minecraft, compartilhado em servidor Discord |
| Nome do arquivo | `mavi-chivas.exe` |
| **MD5** | `71930BF332B6A596468FAC494AD8EA98` |
| **SHA1** | `D18539875B0ECDA428B89807AFA263792D6BEEE1` |
| **SHA256** | `29269FA6CF19C9114B15D13044B64903C33BC382007EC85FAD8A5277711946F4` |
| Data da análise (UTC) | 2026-05-11 03:15 |
| Ambiente | Windows 10 Professional (build 19044, 64-bit) |
| Fonte da submissão | ANY.RUN (sandbox pública), tarefa localizável pelo SHA256 |

### 2.1 Evidência de download e execução

![Aviso do navegador no momento do download](img/01-download-dropbox.png)

*O navegador já sinalizou o arquivo como potencialmente prejudicial no momento do download. O link do Dropbox foi removido da captura.*

![Alerta nativo do Windows — publisher desconhecido](img/02-publisher-desconhecido.png)

*Alerta nativo do Windows: publisher desconhecido, sem assinatura digital válida.*

---

## 3. Metodologia

1. Executei a amostra em ambiente isolado de sandbox (ANY.RUN), com captura de comportamento em tempo real — processos, rede e sistema de arquivos.
2. Fiz triagem complementar de detecção via Microsoft Defender.
3. Extraí os indicadores comportamentais e de rede a partir da execução monitorada.
4. Revisei a lista de **arquivos efetivamente tocados**, não apenas os artefatos de string.
5. Decodifiquei o PowerShell em Base64 da linha de comando, sem executar nada (revisão de 2026-11-15).
6. Mapeei os achados ao framework MITRE ATT&CK, com nível de confiança por técnica.
7. Traduzi o comportamento observado em regras Sigma de detecção (seção 11).

---

## 4. Achados — Análise Comportamental

### 4.1 Uso de utilitários nativos do Windows (Living-off-the-Land)

A amostra executa comandos por `cmd.exe` (`cmd.exe /d /s /c "..."`), observado diversas vezes na árvore de processos. Também identifiquei artefatos de string associados a `waitfor` e `write`, e um indício de uso de **Alternate Data Streams (ADS)**.

```
Observado:   cmd.exe /d /s /c "<comando>"
Artefatos:   "waitfor", "write", indício de ADS
Origem:      árvore de processos (cmd.exe) e INPUT_FILE (strings)
MITRE ATT&CK: T1059.003 (Windows Command Shell)
              T1564.004 (NTFS File Attributes) — indício, ver ressalva
```

> **Ressalva de evidência.** A indicação de ADS e o `waitfor` vêm de **artefato de string**, não de comportamento observado. Classifico o ADS como *indício*, não como técnica confirmada. Validação exigiria análise estática ou inspeção direta dos streams NTFS.

### 4.2 Entrega via Dropbox

```
URL:   hxxps://www.dropbox[.]com/scl/fi/zprhs69ehmegj90tnjdfn/mavi-chivas.exe
Quem baixa:  msedge.exe (o navegador da vítima)
MITRE ATT&CK: T1204.002 (User Execution: Malicious File)
```

**Interpretação.** O executável fica hospedado num serviço de nuvem legítimo e amplamente confiável, que raramente consta em listas de bloqueio. O link é compartilhado no Discord, o navegador da vítima baixa o arquivo e a vítima o executa.

> **Correção (2026-11-15).** A primeira versão tratava o Dropbox como **segundo estágio** baixado por um primeiro estágio, mapeado como `T1105 — Ingress Tool Transfer`. O relatório mostra que a tarefa analisou o próprio link: quem baixa é o `msedge.exe`, e depois da execução não há nenhum download de outro executável. O arquivo do Dropbox é a própria amostra. `T1105` foi descartada.
>
> A sandbox sugeriu `T1567.002 — Exfiltration to Cloud Storage` para esse indicador. Continua descartada: a técnica descreve o canal, não a direção, e aqui o Dropbox só entregou o arquivo.

### 4.3 Indício de atividade de rede — funcionalidade de download

```
Artefato identificado: "downloadFile"

Origem: INPUT_FILE
MITRE ATT&CK: não mapeado automaticamente pela ferramenta
```

**Interpretação.** A presença dessa string é consistente com uso de API de download programático. Na execução analisada, porém, **nenhum download feito pela amostra foi observado** — a única transferência de executável é a do navegador baixando a própria amostra. A string fica como capacidade possível, não como comportamento.

### 4.4 PowerShell codificado e compilação via Add-Type

A amostra inicia o PowerShell **seis vezes**, sempre com todas as flags de silêncio:

```
cmd.exe /d /s /c "Powershell -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -Encoded WwBTAHkA..."
  └─ powershell.exe
       └─ csc.exe /noconfig /fullpaths @"C:\Users\admin\AppData\Local\Temp\<aleatório>.cmdline"
```

Decodifiquei o comando que o relatório exibe por completo. São **duas camadas de Base64**: a primeira (UTF-16) monta e decodifica um JSON com um campo `Script`, que traz a segunda:

```powershell
Add-Type -Name Window -Namespace Console -MemberDefinition '
    [DllImport("Kernel32.dll")]
    public static extern IntPtr GetConsoleWindow();

    [DllImport("user32.dll")]
    public static extern bool ShowWindow(IntPtr hWnd, Int32 nCmdShow);
'
$consolePtr = [Console.Window]::GetConsoleWindow()
#0 hide
[Console.Window]::ShowWindow($consolePtr, 0)
```

**Interpretação.** O `csc.exe` aparece porque o `Add-Type` compila esse C# na hora. O código compilado é um helper de P/Invoke que localiza a janela do console e a esconde (`ShowWindow(..., 0)`), para que a vítima não veja nenhuma janela preta durante a execução.

```
MITRE ATT&CK: T1059.001 (PowerShell)
              T1027.010 (Command Obfuscation) — duas camadas de Base64
              T1564.003 (Hide Artifacts: Hidden Window)
```

> **Correção (2026-11-15).** A primeira versão afirmava que o PowerShell compilava o **código malicioso** em memória (`T1027.004 — Compile After Delivery`), com o binário só existindo depois da compilação. O código decodificado mostra que o que é compilado é apenas o helper que esconde a janela; o stealer chega pronto. `T1027.004` foi descartada para esta amostra.
>
> A primeira versão também falava em "66 ocorrências" de Base64. O relatório mostra **seis** execuções de PowerShell codificado. Decodifiquei a que o relatório exibe por completo; não verifiquei se as outras cinco têm o mesmo conteúdo.
>
> Este comportamento originou as **[regras Sigma 01](sigma/01_powershell_csc_compile.yml)** e **[04](sigma/04_powershell_base64_bypass.yml)**.

### 4.5 Furto confirmado de dados do navegador (Credential Access)

A lista de arquivos tocados durante a execução (*dropped files*, conforme IOCs exportados da sandbox) inclui arquivos internos do Microsoft Edge que armazenam credencial salva, cookie de sessão e histórico:

| Arquivo (Edge) | Conteúdo | Hash (SHA256) |
|:--|:--|:--|
| `Web Data` | Senhas salvas e dados de autofill/cartão | `cdb4ee2a…e1839a8` |
| `Network\Cookies` | Cookies de sessão (sequestro de sessão) | `d05660a0…15159e1d` |
| `History` | Histórico de navegação | `0f7728c8…e8caaa2d` |
| `Last Browser` | Metadado de navegador ativo | `9c70f766…bc137f4` |

**Interpretação — este é o achado que reclassifica a amostra.** O acesso direto ao arquivo `Web Data` (banco SQLite onde o Chromium/Edge armazena senha salva) é **evidência concreta de capacidade de furto de credencial**, não apenas indício textual. O acesso ao `Cookies` permite sequestro de sessão autenticada sem necessidade da senha em si.

Isso reclassifica a amostra de *downloader* para **infostealer confirmado**. O relatório marca os mesmos processos com "Steals credentials from Web Browsers" e "Actions looks like stealing of personal data".

Também identifiquei o encerramento de processos de navegador via `taskkill.exe` **antes** da leitura desses arquivos — o relatório registra isso repetidas vezes — comportamento consistente com a necessidade técnica de liberar o lock de arquivo SQLite mantido pelo navegador em execução.

O relatório ainda aponta que a amostra **carrega DLLs da instalação do Firefox** (consistente com usar a própria biblioteca de criptografia do Firefox para ler logins salvos) e marca **possível furto de carteiras de criptomoeda e de dados de mensageiros**. Esses dois últimos ficam como indício: o relatório público não mostra quais arquivos foram lidos.

```
MITRE ATT&CK: T1555.003 (Credentials from Web Browsers)
              T1539     (Steal Web Session Cookie)
```

> **Nota sobre o `taskkill`.** O encerramento dos navegadores é **etapa habilitadora** do furto de credencial, não ação de impacto ou disrupção. Não o mapeio como `T1489 — Service Stop` (tática Impact), porque a intenção observada não é interromper serviço: é liberar o lock do SQLite para viabilizar `T1555.003`.
>
> Este par de comportamentos originou as **[regras Sigma 02 e 03](sigma/)**.

### 4.6 Nota sobre empacotamento multiplataforma (Node.js/Electron)

Entre os arquivos tocados durante a execução, identifiquei bibliotecas nativas do Node.js (`ref-napi`, `ffi-napi`) com builds para Linux, além de componentes do Electron:

```
...\ref-napi\prebuilds\linux-arm64\electron.napi.armv8.node
...\ffi-napi\prebuilds\linux-x64\node.napi.uv1.node
```

**Interpretação.** `ref-napi` e `ffi-napi` são bibliotecas que permitem chamar APIs nativas do sistema operacional a partir de código Node.js — uso comum em stealers modernos escritos em Node.js/Electron que precisam invocar API do Windows (por exemplo, rotinas de descriptografia DPAPI) para acessar credencial protegida pelo sistema.

A presença de builds para Linux, mesmo em uma amostra Windows, sugere **empacotamento multiplataforma**, onde apenas os binários relevantes à plataforma-alvo são de fato executados. Na sandbox, a amostra rodou como **seis processos** do mesmo executável.

![Múltiplas janelas do mesmo executável](img/03-janelas-electron.png)

*Múltiplas janelas do mesmo executável — assinatura visual comum de aplicações Electron, reforçando a hipótese de empacotamento Node.js/Electron.*

### 4.7 Reconhecimento do ambiente e conexão com o Discord

**Checagem de máquina virtual.** A amostra consulta a chave do registro que guarda as strings da BIOS, onde ambientes virtuais costumam se identificar:

```
cmd.exe /d /s /c "reg query "HKLM\HARDWARE\DESCRIPTION\System""
  └─ reg.exe query "HKLM\HARDWARE\DESCRIPTION\System"
```

O relatório marca todos os processos da amostra com "The process checks if it is being run in the virtual environment".

**Informações do sistema.** Leitura do GUID da máquina, do nome do computador e das informações de CPU.

**IP externo.** Conexão TLS com `myexternalip[.]com` (alerta de rede "IP Check Domain").

**Discord.** Os mesmos processos que leram os dados do navegador abriram conexões TLS com `discord[.]com` (alertas de rede "Observed Discord Domain in TLS SNI"). O conteúdo é criptografado, então não há como mostrar o que foi enviado; mas um stealer se comunicando com o Discord logo depois do furto é o padrão clássico de exfiltração por webhook. Classifico como **provável**.

**Pontos sem evidência no relatório público:**

- Comando classificado como **"AV-Killer"** e tentativa de bypass de UAC: foram registrados na primeira análise a partir da matriz ATT&CK da sandbox, mas não aparecem na lista pública de comportamentos. Ficam como indício.
- Uso de `net.exe`, que a sandbox descreve como consulta a "sessões ativas". Não extraí a linha de comando exata, então a técnica fica como indício.

```
MITRE ATT&CK: T1497.001 (Virtualization/Sandbox Evasion: System Checks)
              T1012     (Query Registry)
              T1082     (System Information Discovery)
              T1016     (System Network Configuration Discovery)
              T1567.004 (Exfiltration Over Webhook) — provável
              T1685     (Disable or Modify Tools) — indício
              T1049     (System Network Connections Discovery) — indício
```

---

## 5. Cadeia de Execução

```
1. Link do Dropbox compartilhado em servidor Discord, apresentado como mod de Minecraft
        |
2. O navegador da vítima baixa mavi-chivas.exe e a vítima executa
        |
3. Seis processos do mesmo executável (Electron)
        |
4. reg query HKLM\HARDWARE\DESCRIPTION\System (checagem de VM)
   + leitura de GUID, nome do computador e CPU
        |
5. PowerShell codificado -> Add-Type -> csc.exe
   (helper que esconde a janela do console)
        |
6. taskkill nos navegadores (libera o lock do SQLite)
        |
7. Leitura de Web Data, Cookies e History do Edge
   (furto confirmado de credencial e sessão)
        |
8. TLS para myexternalip[.]com e discord[.]com
   (IP externo; exfiltração provável por webhook)
```

Os passos **1 a 7** são sustentados por processo, linha de comando ou arquivo tocado observado na execução. O passo **8** tem evidência de conexão, mas não do conteúdo enviado.

---

## 6. Mapeamento MITRE ATT&CK

| Técnica | ID | Tática | Evidência | Confiança |
|:--|:--|:--|:--|:--|
| User Execution: Malicious File | [T1204.002](https://attack.mitre.org/techniques/T1204/002/) | Execution | Vítima executa o mod falso | Confirmada |
| Windows Command Shell | [T1059.003](https://attack.mitre.org/techniques/T1059/003/) | Execution | `cmd.exe /d /s /c` | Confirmada |
| PowerShell | [T1059.001](https://attack.mitre.org/techniques/T1059/001/) | Execution | PowerShell com `-Encoded` | Confirmada |
| Virtualization/Sandbox Evasion: System Checks | [T1497.001](https://attack.mitre.org/techniques/T1497/001/) | Stealth, Discovery | `reg query` nas strings da BIOS | Confirmada |
| Query Registry | [T1012](https://attack.mitre.org/techniques/T1012/) | Discovery | `HKLM\HARDWARE\DESCRIPTION\System` | Confirmada |
| System Information Discovery | [T1082](https://attack.mitre.org/techniques/T1082/) | Discovery | GUID, nome do computador, CPU | Confirmada |
| System Network Configuration Discovery | [T1016](https://attack.mitre.org/techniques/T1016/) | Discovery | `myexternalip[.]com` | Confirmada |
| Command Obfuscation | [T1027.010](https://attack.mitre.org/techniques/T1027/010/) | Stealth | Duas camadas de Base64 | Confirmada |
| Hide Artifacts: Hidden Window | [T1564.003](https://attack.mitre.org/techniques/T1564/003/) | Stealth | `ShowWindow(console, 0)` | Confirmada |
| Credentials from Web Browsers | [T1555.003](https://attack.mitre.org/techniques/T1555/003/) | Credential Access | Leitura de `Web Data` (Edge) | Confirmada |
| Steal Web Session Cookie | [T1539](https://attack.mitre.org/techniques/T1539/) | Credential Access | Leitura de `Network\Cookies` | Confirmada |
| Exfiltration Over Webhook | [T1567.004](https://attack.mitre.org/techniques/T1567/004/) | Exfiltration | TLS para `discord[.]com` após o furto | Provável |
| Disable or Modify Tools | [T1685](https://attack.mitre.org/techniques/T1685/) | Defense Impairment | "AV-Killer" na matriz da primeira análise | **Indício** |
| System Network Connections Discovery | [T1049](https://attack.mitre.org/techniques/T1049/) | Discovery | `net.exe`, "sessões ativas" | **Indício** |
| Hide Artifacts: NTFS File Attributes | [T1564.004](https://attack.mitre.org/techniques/T1564/004/) | Stealth | `cmd.exe` + ADS — **artefato de string** | **Indício** |

**Versão do ATT&CK.** Táticas e IDs conforme o ATT&CK v20. Nessa versão a tática Defense Evasion foi dividida em **Stealth** e **Defense Impairment**, e o antigo `T1562.001 (Impair Defenses: Disable or Modify Tools)` passou a ser `T1685`.

**Sobre a coluna Confiança.** *Confirmada* = sustentada por comportamento, linha de comando ou arquivo tocado observado na execução. *Provável* = há evidência de rede, mas não do conteúdo. *Indício* = sugerida por artefato de string ou por classificação sem evidência pública; exigiria análise estática ou engenharia reversa.

**Técnicas descartadas:**

- `T1027.004 — Compile After Delivery`: o `csc.exe` compila apenas o helper que esconde a janela, não o código malicioso — ver seção 4.4.
- `T1105 — Ingress Tool Transfer`: não há segundo estágio; o navegador baixa a própria amostra — ver seção 4.2.
- `T1567.002 — Exfiltration to Cloud Storage`: sugerida automaticamente pela sandbox para o Dropbox. Descartada por inversão de direção do fluxo — ver seção 4.2.
- `T1489 — Service Stop`: o `taskkill` em navegadores é etapa habilitadora de `T1555.003`, não ação de impacto — ver seção 4.5.
- `T1140 — Deobfuscate/Decode Files`: substituída por `T1027.010`, que descreve melhor um comando ofuscado na própria linha de comando.
- `T1135 — Network Share Discovery`: sem evidência no relatório público.

---

## 7. Indicadores de Comprometimento (IOCs)

### Infraestrutura e amostra

| Tipo | Valor |
|:--|:--|
| URL (entrega) | `hxxps://www.dropbox[.]com/scl/fi/zprhs69ehmegj90tnjdfn/mavi-chivas.exe` |
| Domínio (CDN) | `uc09bdccd2cb3e37ded48fbe051e[.]dl.dropboxusercontent[.]com` |
| Nome de arquivo | `mavi-chivas.exe` |
| MD5 | `71930BF332B6A596468FAC494AD8EA98` |
| SHA1 | `D18539875B0ECDA428B89807AFA263792D6BEEE1` |
| SHA256 | `29269FA6CF19C9114B15D13044B64903C33BC382007EC85FAD8A5277711946F4` |

### Rede contatada pela amostra

| Tipo | Valor | Observação |
|:--|:--|:--|
| Domínio | `myexternalip[.]com` | Serviço legítimo de consulta de IP |
| Domínio | `discord[.]com` | Serviço legítimo; não bloquear por domínio |

### Artefatos de comportamento

| Tipo | Valor |
|:--|:--|
| Linha de comando | `cmd.exe /d /s /c "reg query "HKLM\HARDWARE\DESCRIPTION\System""` |
| Linha de comando | `powershell -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -Encoded ...` |
| String | `waitfor` |
| String | `cmd.exe` (indício de ADS) |
| String | `downloadFile` |

> Os hashes dos arquivos de navegador visados (`Web Data`, `Cookies`, `History`) constam na seção 4.5, categorizados separadamente — **são arquivos da vítima, não infraestrutura do atacante**, e não devem ser tratados como IOC de detecção.

---

## 8. Classificação de Risco

| | |
|:--|:--|
| **Classificação** | Malicioso — Infostealer (Credential Stealer), confirmado |
| **Impacto potencial** | **Alto** — furto confirmado de credencial salva, cookie de sessão e histórico |
| **Complexidade de distribuição** | Baixa — engenharia social simples, isca de mod de jogo via Discord |
| **Público-alvo típico** | Jogadores / comunidade gamer, frequentemente menores de idade |

> [!IMPORTANT]
> **Em caso de infecção confirmada, remover o arquivo não é suficiente.** Como houve furto de credencial salva e de cookie de sessão, os dados já saíram da máquina. A resposta exige **rotação das senhas armazenadas no navegador** e **invalidação das sessões ativas** (logout de todos os dispositivos) nas contas afetadas. Cookie de sessão roubado permite acesso mesmo depois da troca de senha, até que a sessão seja invalidada.

---

## 9. Lições Aprendidas

1. **A classificação inicial estava incompleta.** Revisando os arquivos efetivamente tocados pela amostra — não só as strings — descobri que "downloader" era leitura parcial. O acesso a `Web Data` e `Cookies` é o que confirma capacidade de furto de credencial. Comportamento parcial gera classificação parcial.

2. **Decodifique antes de concluir.** "PowerShell compilando C#" parecia payload em memória. Decodificado, era um helper que esconde a janela. A primeira versão deste relatório errou exatamente nisso.

3. **Classificação automática de sandbox é ponto de partida, não conclusão.** A ferramenta mapeou o Dropbox como exfiltração quando o fluxo era de entrada. Ela acertou o canal e errou a direção.

4. **"Vi uma string" e "confirmei um comportamento" são níveis de evidência diferentes** — e o relatório precisa dizer qual é qual. Foi por aplicar esse critério que o ADS ficou marcado como indício, e não como técnica confirmada.

5. **Regra de detecção também precisa ser testada contra a amostra.** A regra 04 procurava `-EncodedCommand`, `-enc` e `-e`, mas a amostra usa `-Encoded`. Sem testar contra o comando real, a regra não dispararia na própria amostra que a originou.

6. **Serviços legítimos de nuvem e comunicação** (Dropbox, Discord) são vetores recorrentes, justamente por raramente serem bloqueados. Aqui o Discord aparece nas duas pontas: na isca e, provavelmente, na saída dos dados.

7. **Nem todo indicador de rede é malicioso.** Boa parte do tráfego capturado era telemetria legítima do Edge. Saber o que descartar é parte do trabalho.

8. **Comunidades de jogos** (mods, cheats, cracks, servidores de Discord) seguem sendo vetor de distribuição relevante e frequentemente subestimado.

---

## 10. Conclusão

A amostra analisada apresenta padrão comportamental de **infostealer confirmado**: distribuição via engenharia social em comunidade de jogos, checagem de ambiente virtual, PowerShell ofuscado que esconde a própria janela, encerramento dos navegadores para liberar os bancos SQLite, furto confirmado de credencial salva, cookie de sessão e histórico, e conexão provável de exfiltração com o Discord.

Essa investigação, conduzida com ferramentas de sandbox dinâmico e revisão de indicadores de comprometimento, foi um dos meus primeiros exercícios práticos em análise de malware. A revisão posterior, com o relatório público e o PowerShell decodificado, corrigiu três conclusões da primeira versão — e é parte do aprendizado.

---

## 11. Regras de Detecção — Sigma

Traduzi o comportamento observado em quatro regras [Sigma](https://sigmahq.io/), no diretório [`sigma/`](sigma/). Todas validadas com o parser oficial `pySigma`.

A evidência que eu **de fato** tenho é comportamental: cadeia de processos, argumentos de linha de comando e arquivos acessados. Isso é o domínio do Sigma, que expressa detecção sobre log e converte para Wazuh, Splunk, Elastic e outros back-ends.

| # | Regra | Técnica | Nível |
|:--|:--|:--|:--|
| 01 | [Compilação C# via PowerShell (Add-Type)](sigma/01_powershell_csc_compile.yml) | T1564.003, T1027.004 | medium |
| 02 | [Encerramento de múltiplos navegadores via taskkill](sigma/02_taskkill_multiplos_navegadores.yml) | T1555.003 | medium |
| 03 | [Acesso a banco de credenciais por processo não-navegador](sigma/03_acesso_credenciais_navegador.yml) | T1555.003, T1539 | high |
| 04 | [PowerShell com bypass de ExecutionPolicy e Base64](sigma/04_powershell_base64_bypass.yml) | T1027.010, T1059.001 | high |

### Notas de implantação

**As regras 02 e 03 valem mais juntas.** Isoladamente, encerrar um navegador é ação comum e o acesso a arquivo de perfil tem usos legítimos. O sinal forte é a **sequência**: `taskkill` nos navegadores seguido, em segundos, de leitura dos bancos SQLite de credencial por um processo que não é navegador. Se o seu SIEM suporta correlação temporal, essa é a regra que importa.

**A regra 03 exige auditoria de acesso a objeto.** O Sysmon Event ID 11 registra *criação* de arquivo, não leitura. Para capturar a leitura é preciso SACL configurada nas pastas de perfil do navegador (gerando Event ID 4663) ou um EDR que emita evento de leitura. Sem isso a regra não dispara.

**A regra 04 foi corrigida em 2026-11-15.** O PowerShell aceita qualquer prefixo de `-EncodedCommand`, e a amostra usa `-Encoded`, que a versão anterior não cobria. A regra agora procura os prefixos (`-e`, `-en`, `-enc`, `-enco...`, `-ec`).

**Todas estão marcadas como `experimental`.** Não foram validadas contra telemetria de produção — foram derivadas do comportamento documentado nesta análise. Faça tuning no seu ambiente antes de promover a `stable`, especialmente a regra 01, que tem falso positivo legítimo em scripts que usam `Add-Type` e em máquinas de desenvolvimento .NET.

### Conversão

```bash
pip install sigma-cli pysigma-backend-splunk
sigma convert -t splunk -p sysmon sigma/
```

Substitua o back-end conforme a plataforma: `pysigma-backend-elasticsearch`, `pysigma-backend-opensearch`, entre outros.

---

<sub>Análise pessoal e educacional. Nenhuma amostra executável é hospedada neste repositório. IOCs defangados conforme convenção de segurança. Conteúdo sob licença <a href="LICENSE">CC BY 4.0</a>.</sub>
