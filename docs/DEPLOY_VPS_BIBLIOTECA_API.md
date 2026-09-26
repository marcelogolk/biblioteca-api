# Deploy da Biblioteca API — Oracle Cloud VPS

Documento de referência com todas as decisões, justificativas e passos práticos para provisionar e configurar a VPS do zero. Serve tanto como registro histórico quanto como roteiro para refazer o processo, caso necessário.

**Status atual:** Camadas 0 a 5 concluídas. Camada 6 (containerizar a API) e Camada 7 (expor à internet) pendentes.

---

## Mapa geral das camadas

| Camada | Descrição | Status |
|---|---|---|
| 0 | Provisionamento da instância (Oracle) | ✅ Concluída |
| 1 | Hardening de SSH | ✅ Concluída |
| 2 | Firewall (rede) | ✅ Concluída |
| 3 | Usuário não-root para o dia a dia | ✅ Concluída |
| 4 | Docker na VPS | ✅ Concluída |
| 5 | Postgres em container | ✅ Concluída (validada localmente) |
| 6 | Build e execução da API Quarkus em container | ⏳ Pendente |
| 7 | Expor a API à internet (reverse proxy + TLS) | ⏳ Pendente |

---

## Camada 0 — Provisionamento da instância

### Decisões tomadas

- **Provedor:** Oracle Cloud Infrastructure (OCI), tier Always Free.
- **Região:** Brazil East (São Paulo) — `sa-saopaulo-1`. Existe uma segunda região no Brasil (Vinhedo) que pode ser usada como alternativa em caso de indisponibilidade de capacidade.
- **Shape:** `VM.Standard.A1.Flex` (Ampere ARM).
  - Escolhido em vez do `VM.Standard.E2.1.Micro` (AMD, 1 GB RAM) pela folga de recursos.
  - **Atenção:** a Oracle reduziu a cota gratuita do A1.Flex de 4 OCPU/24 GB para **2 OCPU / 12 GB** em meados de 2026. A documentação pode estar desatualizada nos tutoriais mais antigos — confira sempre o valor atual no console.
  - Risco de incompatibilidade de arquitetura (ARM vs x86) é mitigado porque as imagens oficiais usadas no projeto (Postgres, Java/`eclipse-temurin`) são multi-arch. O risco residual fica concentrado no processo de build de imagens próprias (ver Camada 6).
- **Imagem:** Canonical Ubuntu 24.04 **Minimal** (aarch64).
  - LTS (suporte de 5 anos), maduro (não é a versão mais recente lançada).
  - Minimal escolhido para instalar apenas o necessário, sob controle consciente.
  - **Trade-off aceito:** imagens Minimal em ARM não suportam **Instância Blindada** (Secure Boot/Measured Boot/TPM). Decisão consciente: priorizar a imagem enxuta em vez dessa camada extra de defesa em profundidade (que protege contra um cenário de ataque que já pressupõe outro comprometimento anterior).
- **IMDS:** IMDSv2 obrigatório (exigir cabeçalho de autorização) — mitiga SSRF básico.
- **Script de inicialização (cloud-init):** deixado em branco propositalmente, para aprender cada passo manualmente antes de automatizar no futuro.
- **Migração ao vivo:** padrão (Oracle escolhe a melhor opção).
- **Restaurar estado após manutenção:** ativado (a instância deve rodar 24/7).
- **Oracle Cloud Agent (plugins):** apenas "Monitoramento de Instância de Computação" ativado. Bastion e demais plugins corporativos desativados (fora de escopo).
- **Atributos de segurança (ZPR):** nenhum — complexidade desnecessária para uma única instância.
- **Rede:** nova VCN e nova sub-rede pública criadas junto com a instância (CIDR `10.0.0.0/24`).
- **IPv6:** não utilizado.
- **IP público:** tipo **efêmero** (gratuito, mas muda se a instância for parada e iniciada — não muda em reboots normais do SO). Decisão consciente, sabendo do trade-off frente ao IP reservado (também gratuito, mas fixo).
- **Armazenamento:** volume de boot de **100 GB** (decisão consciente, acima do padrão de 46.6 GB), com criptografia em trânsito ativada. Chave de criptografia gerenciada pela Oracle (não por chave própria).
- **Chave SSH:** gerada localmente no Mint (`ssh-keygen -t ed25519`), com passphrase, dedicada exclusivamente a esta VPS (não reaproveitada do GitHub/Tailscale).

### Passo a passo resumido

1. Console Oracle → Compute → Instances → Create Instance.
2. Nome da instância, compartimento padrão.
3. Alterar imagem → Canonical Ubuntu 24.04 Minimal (aarch64).
4. Alterar forma → `VM.Standard.A1.Flex` → expandir e ajustar OCPU/memória para o teto gratuito atual.
5. Gerenciamento → ativar cabeçalho de autorização do IMDS; deixar script de inicialização em branco; desmarcar plugins desnecessários do Cloud Agent (manter só monitoramento).
6. Configuração de disponibilidade → deixar migração ao vivo no padrão; ativar "restaurar após manutenção".
7. Segurança → Instância Blindada (opcional — não suportada em Minimal ARM).
8. Rede → criar nova VCN e nova sub-rede pública; preencher Bloco CIDR (ex: `10.0.0.0/24`); IPv4 público fica pendente para configurar após a criação (limitação da tela simplificada).
9. Chaves SSH → colar/upload da chave pública já gerada localmente.
10. Armazenamento → definir tamanho do volume de boot; ativar criptografia em trânsito.
11. Revisar tudo e criar.
12. **Após a criação:** atribuir IP público manualmente (Instância → Rede → VNIC → Administração do IP → Designar IP público efêmero ou reservado). Alternativamente, usar o assistente "Conectar sub-rede pública à internet" (cria Internet Gateway, rota e NSG automaticamente).

---

## Camada 1 — Hardening de SSH

### Decisões e justificativas

1. **Autenticação apenas por chave** (`PasswordAuthentication no`) — elimina o vetor de força bruta por senha.
2. **Root desabilitado via SSH** (`PermitRootLogin no`) — defesa em profundidade; mesmo com chave comprometida, o atacante cairia em usuário sem privilégio automático.
3. **Porta customizada** (`51022` no lugar de `22`) — reduz ruído de bots de varredura em massa (não é segurança real, é higiene operacional).
4. **`fail2ban`** — monitora falhas de autenticação e bane IPs automaticamente. Configurado para a porta customizada, com `findtime` reduzido e `bantime` aumentado para reduzir a janela de tentativas de bots.
5. **`PubkeyAuthentication yes`** e **`KbdInteractiveAuthentication no`** explicitados no arquivo (evita depender de defaults implícitos da imagem que podem mudar).
6. **Console Serial da Oracle configurado como plano de contingência** — via independente do SSH, útil se um autobanimento ou erro de configuração travar o acesso normal. Requer chave **RSA** específica (diferente da chave ED25519 usada no SSH normal).

### Usuário administrativo

- Usuário `marcelo` criado com `sudo adduser marcelo` + `sudo usermod -aG marcelo sudo`.
- Chave pública copiada do usuário padrão da imagem (`ubuntu`) para `~/.ssh/authorized_keys` de `marcelo`, com permissões corrigidas (`chown`, `chmod 700` no diretório, `chmod 600` no arquivo).

### Passo a passo — configuração do `/etc/ssh/sshd_config`

```bash
sudo nano /etc/ssh/sshd_config
```

Ajustar/descomentar:
```
PasswordAuthentication no
PermitRootLogin no
PubkeyAuthentication yes
KbdInteractiveAuthentication no
Port 51022
```

Validar sintaxe antes de reiniciar:
```bash
sudo sshd -t
```

**Atenção — Ubuntu 24.04 usa socket activation.** Alterar `Port` no `sshd_config` não é suficiente; é necessário também sobrescrever o socket do systemd:

```bash
sudo systemctl edit ssh.socket
```
Conteúdo do override:
```ini
[Socket]
ListenStream=
ListenStream=51022
```
```bash
sudo systemctl daemon-reload
sudo systemctl restart ssh.socket
sudo systemctl restart ssh.service
```

**Procedimento de segurança obrigatório para qualquer mudança em SSH/firewall:** manter a sessão atual aberta e testar a mudança em uma **segunda sessão nova**, só fechando a original após confirmar que a nova conexão funciona.

### Configuração do `fail2ban`

Nunca editar `/etc/fail2ban/jail.conf` diretamente (é sobrescrito em atualizações). Criar `jail.local`:

```bash
sudo nano /etc/fail2ban/jail.local
```
```ini
[sshd]
enabled = true
port = 51022
maxretry = 5
findtime = 5m
bantime = 4h
```
```bash
sudo systemctl restart fail2ban
sudo systemctl enable fail2ban
```

Verificação:
```bash
sudo fail2ban-client status sshd
```

### Console Serial (contingência)

1. Gerar chave RSA dedicada: `ssh-keygen -t rsa -b 2048 -C "console-serial" -f ~/.ssh/id_rsa_console_biblioteca`
2. Console Oracle → Instância → Recursos → Conexão da console → Criar conexão local → upload da chave pública RSA.
3. Aguardar status "Ativo". Comando de conexão disponível no menu de Ações da conexão criada.

---

## Camada 2 — Firewall

### As 4 camadas reais de firewall (não confundir com autenticação/detecção, que são categorias diferentes)

1. **NSG** (Network Security Group, nível de VNIC) — Oracle.
2. **Security List** (nível de sub-rede) — Oracle.
3. **`iptables` hardcoded** — já vem na imagem Oracle, arquivo `/etc/iptables/rules.v4`, carregado por mecanismo próprio da imagem (não é o `iptables-persistent` padrão do Ubuntu).
4. **`ufw`** — instalado manualmente (`sudo apt install ufw`), persistência própria via `systemctl enable ufw` e arquivos internos em `/etc/ufw/`.

### Regras importantes descobertas na prática

- **NSG e Security List são avaliados em lógica "OU"**: basta uma das duas permitir o tráfego para ele passar. Não é necessário duplicar regras nas duas, mas foi feito por clareza/redundância.
- **Dentro do `iptables`/`ufw`, a ordem das regras importa** — processamento é sequencial, para na primeira regra que der match. A regra hardcoded da Oracle (liberando porta 22) e o `REJECT` catch-all vêm **antes** das cadeias do `ufw` — por isso o `ufw` sozinho não bastava para liberar a porta customizada; foi necessário também editar `/etc/iptables/rules.v4`.
- **Egress (saída) já vem liberado por padrão** em todas as camadas Oracle (regra `0.0.0.0/0`, todos os protocolos) e no `ufw` (`allow outgoing`). Não expor porta de saída não é necessário mexer.
- **Regras de ICMP tipo 3 (Destination Unreachable) devem permanecer liberadas** — não são sobre "ping", mas sobre Path MTU Discovery. Bloqueá-las causa falhas silenciosas de conexão com pacotes grandes.

### Estado final (porta 51022 liberada, porta 22 fechada, em todas as camadas)

```bash
# ufw
sudo ufw allow 51022/tcp
sudo ufw delete allow 22/tcp
sudo ufw enable
sudo ufw status verbose

# iptables hardcoded — editar manualmente /etc/iptables/rules.v4
# adicionar (antes da linha REJECT):
#   -A INPUT -p tcp -m state --state NEW -m tcp --dport 51022 -j ACCEPT
# remover a linha equivalente da porta 22
sudo nano /etc/iptables/rules.v4
# testar ao vivo antes de persistir:
sudo iptables -I INPUT <posição> -p tcp -m state --state NEW -m tcp --dport 51022 -j ACCEPT
sudo iptables -D INPUT -p tcp -m state --state NEW -m tcp --dport 22 -j ACCEPT
# confirmar que o arquivo bate com o estado ao vivo:
sudo iptables-save | diff - /etc/iptables/rules.v4
```

Na Oracle: NSG e Security List — remover regra de entrada porta 22, manter/adicionar regra de entrada porta 51022 (CIDR `0.0.0.0/0`, TCP).

---

## Camada 3 — Usuário não-root

- Usuário `marcelo`, criado via `adduser`, com `sudo`, autenticação por chave SSH (mesma chave ED25519 usada para acesso geral).
- Verificações: `sudo whoami` (deve pedir senha local e retornar `root`), `groups marcelo` (deve incluir `sudo`).

---

## Camada 4 — Docker

Instalado a partir do **repositório oficial da Docker Inc.** (não o pacote `docker.io` do Ubuntu), por ser mais atualizado.

```bash
for pkg in docker.io docker-doc docker-compose podman-docker containerd runc; do sudo apt remove $pkg; done

sudo apt update
sudo apt install ca-certificates curl -y
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y
```

O comando de repositório detecta automaticamente a arquitetura (`arm64` nesta VPS) via `dpkg --print-architecture` — não fixar `amd64` manualmente.

**Rodar Docker sem `sudo`** (decisão consciente, equivalente a acesso root — aceito por ser o único operador da máquina):
```bash
sudo usermod -aG docker marcelo
# necessário novo login (ou `newgrp docker`) para ter efeito
```

Verificação:
```bash
docker run hello-world
docker --version
docker compose version
```

---

## Camada 5 — Postgres em container

### Decisões de arquitetura

- **Volume:** *named volume* do Docker (não bind mount) — padrão recomendado, e como único operador da VPS não há ganho real em customizar o caminho manualmente.
- **Usuário do banco:** dois usuários separados dentro do Postgres:
  - `POSTGRES_ADMIN_USER` (superusuário, criado automaticamente pela imagem via `POSTGRES_USER`/`POSTGRES_PASSWORD`) — usado só para administração/manutenção.
  - `APP_DB_USER` (usuário de aplicação, criado via script de inicialização, dono do banco `biblioteca_api`, **sem** privilégios de superusuário) — usado pela API no dia a dia. Princípio de *least privilege*: se a aplicação for comprometida, o dano fica limitado ao que esse usuário pode fazer.
  - **Pendência registrada para a Fase 2:** ao introduzir Flyway, separar ainda mais em usuário de *migração* (com DDL) e usuário de *runtime* (só CRUD).
- **Credenciais:** nunca em texto puro no `docker-compose.yml` — usar arquivo `.env` (nunca commitado) + `.env.example` (commitado, com placeholders).

### Estrutura de arquivos

```
projeto/
├── local/
│   ├── docker-compose.yml   (com porta 5432 exposta — API roda fora do Docker localmente)
│   ├── .env
│   ├── .env.example
│   └── init/
│       └── 01-create-app-user.sh
├── vps/
│   ├── docker-compose.yml   (sem porta exposta — API e Postgres no mesmo compose)
│   ├── .env
│   ├── .env.example
│   └── init/
│       └── 01-create-app-user.sh
```

`.gitignore` (na raiz do projeto):
```
# Environment files (local, vps, etc.) — never commit
**/.env
```

### `docker-compose.yml` (versão local, com porta exposta)

```yaml
services:
  postgres:
    image: postgres:16.4
    container_name: biblioteca_api_postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_ADMIN_USER}
      POSTGRES_PASSWORD: ${POSTGRES_ADMIN_PASSWORD}
      APP_DB_USER: ${APP_DB_USER}
      APP_DB_PASSWORD: ${APP_DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - ./init:/docker-entrypoint-initdb.d:ro
      - biblioteca_api_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_ADMIN_USER} -d ${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

volumes:
  biblioteca_api_data:
```

Na versão da VPS (Camada 6, a integrar): remover o bloco `ports` — comunicação interna entre containers acontece pelo nome do serviço (`postgres:5432`) via rede interna automática do Compose, sem necessidade de expor porta ao host.

### `.env` / `.env.example`

```
# .env (nunca commitado)
POSTGRES_DB=biblioteca_api
POSTGRES_ADMIN_USER=bibliotecario.admin
POSTGRES_ADMIN_PASSWORD=<gerado com: openssl rand -base64 24>
APP_DB_USER=biblioteca_app
APP_DB_PASSWORD=<gerado com: openssl rand -base64 24, diferente da anterior>
```

```
# .env.example (commitado)
POSTGRES_DB=biblioteca_api
POSTGRES_ADMIN_USER=troque_aqui
POSTGRES_ADMIN_PASSWORD=troque_aqui
APP_DB_USER=troque_aqui
APP_DB_PASSWORD=troque_aqui
```

**Nunca reutilizar a mesma senha entre ambiente local e produção, nem entre usuário admin e usuário de aplicação.**

### Script de inicialização (`init/01-create-app-user.sh`)

```bash
#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    CREATE ROLE ${APP_DB_USER} WITH LOGIN PASSWORD '${APP_DB_PASSWORD}';
    ALTER DATABASE ${POSTGRES_DB} OWNER TO ${APP_DB_USER};
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    GRANT ALL PRIVILEGES ON SCHEMA public TO ${APP_DB_USER};
EOSQL
```

**Pontos de atenção aprendidos na prática:**
- A imagem oficial do Postgres **já cria o banco automaticamente** (via `POSTGRES_DB`) antes de qualquer script em `docker-entrypoint-initdb.d/` rodar — por isso o script usa `ALTER DATABASE ... OWNER TO`, não `CREATE DATABASE`.
- `set -e` interrompe o script inteiro no primeiro erro — um `CREATE DATABASE` falhando (banco já existente) pode impedir que o `GRANT` seguinte seja executado, deixando o usuário de aplicação sem privilégios, sem aviso claro.
- Scripts de init só rodam quando o **volume está vazio** (primeira inicialização). Para reprocessar após corrigir o script: `docker compose down -v` (remove o volume) antes de subir de novo.
- Dar permissão de execução ao script: `chmod +x init/01-create-app-user.sh` — sem isso, é ignorado silenciosamente.
- Se o diretório `init/` for criado automaticamente pelo Docker (bind mount para pasta inexistente), ele nasce com dono `root:root`. Prefira criar a pasta manualmente antes (`mkdir -p init`) para que fique com o dono correto (seu usuário).

### `application.properties` com perfis (dev/prod)

```properties
# ---------- Comum a todos os ambientes ----------
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${APP_DB_USER}
quarkus.datasource.password=${APP_DB_PASSWORD}
quarkus.hibernate-orm.log.sql=true
quarkus.rest.path=/api

# ---------- Perfil dev (ambiente local) ----------
%dev.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/biblioteca_api
%dev.quarkus.hibernate-orm.database.generation=update

# ---------- Perfil prod (VPS) ----------
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/biblioteca_api
%prod.quarkus.hibernate-orm.database.generation=update
```

**Pendência registrada:** `database.generation=update` em produção é um risco aceito conscientemente para a v1.0 (qualquer deploy pode alterar o schema real automaticamente). Revisar na Fase 2, junto com a introdução do Flyway — trocar para `validate` em produção e deixar migrations explícitas cuidarem de mudanças de schema.

### Executando localmente

O Quarkus **não lê `.env` automaticamente** como o Docker Compose lê — é necessário carregar as variáveis no shell antes de rodar o Maven:

```bash
docker compose up -d
docker compose logs -f postgres   # confirmar CREATE ROLE / ALTER DATABASE / GRANT sem erros

set -a
source .env
set +a
./mvnw quarkus:dev
```

Verificação manual do banco (usar nome literal do usuário, já que variáveis do `.env` não existem automaticamente no shell do host):
```bash
docker compose exec postgres psql -U bibliotecario.admin -d postgres -c "\du"
docker compose exec postgres psql -U bibliotecario.admin -d postgres -c "\l"
```
Confirmar: role `biblioteca_app` existe sem atributos de superusuário; banco `biblioteca_api` tem `biblioteca_app` como Owner.

**Status: testado e validado localmente com sucesso — API conectando, Swagger funcional.**

---

## Decisões de arquitetura de front-end (registrado, sem ação pendente)

- Front-end futuro (Fase 4) provavelmente será um SPA (React/Vue) compilado como arquivos estáticos, servido por Nginx dentro de um container — não um processo Node.js contínuo (SSR).
- A mesma peça de infraestrutura Nginx planejada para a Camada 7 (reverse proxy da API) poderá futuramente também servir os arquivos estáticos do front, sem retrabalho.

---

## Pendências gerais para as próximas sessões

1. **Camada 6:** escrever o Dockerfile da API Quarkus (modo JVM), integrar ao `docker-compose.yml` da VPS (sem expor porta do Postgres, comunicação via nome do serviço `postgres`), replicar a estrutura `.env`/`init/` para a pasta `vps/`.
2. **Camada 7:** configurar reverse proxy (Nginx), HTTPS via Let's Encrypt usando o domínio `marcelocarvalho.dev.br` (registro DNS tipo A apontando para o IP público da VPS).
3. Revisar `hibernate-orm.database.generation` em produção ao introduzir Flyway (Fase 2).
4. Implementar regra de exclusão de proprietário com livros associados (409 em vez de 500 cru) — pendência já registrada no documento original do projeto.
5. Endpoints de busca de livro por categoria/proprietário — pendência já registrada no documento original do projeto.
6. Testes automatizados (meta de 70% de cobertura) — pendência já registrada no documento original do projeto.


[Voltar ao README](../README.md)