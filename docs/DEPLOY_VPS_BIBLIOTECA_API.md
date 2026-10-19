# Deploy da Biblioteca API - Oracle Cloud VPS

Documento de referência do provisionamento, configuração, implantação e exposição pública da Biblioteca API na Oracle Cloud Infrastructure (OCI). Registra decisões técnicas, estado atual, validações executadas e procedimentos operacionais para reconstrução do ambiente.

## Estado atual

**Camadas 0 a 7 concluídas.**

A Biblioteca API está executando na VPS Oracle Cloud, com a aplicação Quarkus e o PostgreSQL em containers Docker. A comunicação entre os containers, os endpoints REST e a persistência de dados foram validados. A API também está publicada por domínio próprio, com Nginx como proxy reverso e HTTPS configurado.

**Endereço público validado:**

```text
https://api.marcelocarvalho.dev.br/api
```

A aplicação ainda não possui frontend. O domínio raiz permanece disponível para uso futuro.

---

## Mapa geral das camadas

| Camada | Descrição | Status |
|---|---|---|
| 0 | Provisionamento da instância Oracle Cloud |✅Concluída |
| 1 | Hardening de SSH |✅Concluída |
| 2 | Firewall e regras de rede |✅Concluída |
| 3 | Usuário não-root para administração |✅Concluída |
| 4 | Docker e Docker Compose na VPS |✅Concluída |
| 5 | PostgreSQL em container |✅Concluída |
| 6 | Build e execução da API Quarkus em container |✅Concluída e validada na VPS |
| 7 | Exposição pública com domínio, Nginx e HTTPS |✅Concluída e validada |

---

# Camada 0 - Provisionamento da instância

## Decisões adotadas

- Provedor: Oracle Cloud Infrastructure, utilizando recursos do Free Tier.
- Região: Brazil East, São Paulo (`sa-saopaulo-1`).
- Shape: `VM.Standard.A1.Flex`, arquitetura ARM64/Ampere.
- Sistema operacional: Ubuntu 24.04 Minimal para ARM64.
- Rede: VCN e sub-rede pública próprias.
- IPv6: não utilizado nesta implantação.
- IMDSv2: cabeçalho de autorização obrigatório.
- Cloud-init: não utilizado, para que a configuração inicial fosse executada e compreendida manualmente.
- Oracle Cloud Agent: mantido apenas o monitoramento da instância.
- Armazenamento: volume de boot de 100 GB, com criptografia em trânsito habilitada.
- Acesso administrativo: chave SSH ED25519 dedicada à VPS.
- Recuperação: Console Serial configurado como alternativa ao SSH.

## Validações

- Instância criada e iniciada corretamente.
- Acesso SSH confirmado.
- Conectividade de saída confirmada.
- Arquitetura ARM64 considerada no build da imagem da aplicação.

---

# Camada 1 - Hardening de SSH

## Configuração aplicada

- Autenticação somente por chave pública.
- Autenticação por senha desabilitada.
- Login direto de `root` desabilitado.
- Autenticação interativa por teclado desabilitada.
- Porta SSH alterada de 22 para 51022.
- Fail2ban habilitado para o serviço SSH.
- Console Serial mantido como contingência.

Configuração principal:

```text
PasswordAuthentication no
PermitRootLogin no
PubkeyAuthentication yes
KbdInteractiveAuthentication no
Port 51022
```

No Ubuntu 24.04, a alteração também foi aplicada ao `ssh.socket` por causa da ativação do SSH via socket do systemd.

## Procedimento de segurança

Toda alteração de SSH ou firewall deve ser testada em uma segunda sessão antes do encerramento da sessão atual.

---

# Camada 2 - Firewall e rede

## Camadas de filtragem utilizadas

- Network Security Group da Oracle Cloud.
- Security List da sub-rede.
- Regras persistidas de iptables no Ubuntu da imagem Oracle.
- UFW no sistema operacional.

## Portas previstas no estado final

| Porta | Uso | Exposição |
|---|---|---|
| 51022/TCP | SSH administrativo | Pública |
| 80/TCP | HTTP, redirecionamento e validação de certificado | Pública |
| 443/TCP | HTTPS da API | Pública |
| 8080/TCP | Aplicação Quarkus | Não deve ser acessível externamente |
| 5432/TCP | PostgreSQL | Não exposta publicamente |
| 22/TCP | SSH padrão | Fechada |

## Observações aprendidas

- A ordem das regras de iptables importa.
- As permissões necessárias devem estar posicionadas antes do `REJECT` final.
- Regras de ICMP necessárias ao Path MTU Discovery devem ser preservadas.
- O bloqueio externo da porta 8080 foi validado a partir da máquina de desenvolvimento.

---

# Camada 3 - Usuário administrativo não-root

- Usuário administrativo `marcelo` criado na VPS.
- Usuário incluído no grupo `sudo`.
- Chave SSH configurada no diretório `~/.ssh`.
- Permissões de arquivo e diretório ajustadas.
- Operações diárias executadas com usuário não-root e elevação por `sudo` somente quando necessária.

---

# Camada 4 - Docker

Docker instalado a partir do repositório oficial da Docker Inc., com os componentes:

- Docker Engine.
- Docker CLI.
- containerd.
- Docker Buildx.
- Docker Compose Plugin.

A instalação detecta automaticamente a arquitetura ARM64 da VPS.

O usuário administrativo foi adicionado ao grupo `docker`. Essa decisão permite executar Docker sem `sudo`, reconhecendo que a participação nesse grupo equivale a privilégios elevados sobre o host.

## Validações

```bash
docker --version
docker compose version
docker run hello-world
```

---

# Camada 5 - PostgreSQL em container

## Decisões de arquitetura

- Imagem: PostgreSQL 16.4.
- Persistência: volume nomeado do Docker.
- Banco: `biblioteca_api`.
- Usuário administrativo separado do usuário utilizado pela aplicação.
- Credenciais fornecidas por arquivo `.env`, não embutidas no Compose.
- PostgreSQL sem publicação da porta 5432 na VPS.
- Healthcheck com `pg_isready`.

## Papéis do banco

- `POSTGRES_ADMIN_USER`: administração e inicialização.
- `APP_DB_USER`: usuário usado pela Biblioteca API.

O usuário da aplicação não é superusuário. Na Fase 2, com a introdução do Flyway, está prevista a separação adicional entre usuário de migração e usuário de runtime.

## Inicialização

O script `init/01-create-app-user.sh` cria a role da aplicação, altera o proprietário do banco já criado pela imagem oficial do PostgreSQL e concede acesso ao schema `public`.

Ponto importante: scripts em `/docker-entrypoint-initdb.d` são executados somente na primeira inicialização de um volume vazio.

## Validações concluídas

- Role da aplicação criada.
- Banco atribuído ao proprietário esperado.
- API autenticada com o usuário da aplicação.
- Persistência confirmada após reinicialização dos containers sem remoção do volume.

---

# Camada 6 - API Quarkus em container

## Status

**Concluída e validada na Oracle Cloud VPS.**

A aplicação Quarkus está executando em container juntamente com o PostgreSQL, sob gerenciamento do Docker Compose.

## Build e runtime

- Java de compilação: Java 21.
- Runtime do container: Java 21.
- Arquitetura da imagem: ARM64.
- Imagem utilizada na VPS: `biblioteca-api:arm64`.
- Perfil Quarkus: `prod`.

A imagem ARM64 foi empacotada e disponibilizada para execução na VPS. O arquivo `biblioteca-api-arm64.tar` representa o artefato de transporte da imagem utilizado no processo de implantação.

## Configuração da aplicação

O perfil de produção conecta ao PostgreSQL pelo nome do serviço Docker:

```properties
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://postgres:5432/biblioteca_api
%prod.quarkus.hibernate-orm.database.generation=update
```

As credenciais são recebidas pelas variáveis:

```text
APP_DB_USER
APP_DB_PASSWORD
```

## SQL logging
O SQL logging do Hibernate é configurado por perfil:

´´´.properties
%dev.quarkus.hibernate-orm.log.sql=true
%test.quarkus.hibernate-orm.log.sql=false
%prod.quarkus.hibernate-orm.log.sql=false
´´´

## Docker Compose da VPS

O Compose contém dois serviços:

1. `postgres`, responsável pelo banco de dados.
2. `biblioteca-api`, responsável pela aplicação Quarkus.

A API só inicia após o healthcheck do PostgreSQL indicar que o banco está saudável.

## Validações executadas

- Inicialização dos dois containers.
- Conexão da API com o PostgreSQL.
- CRUD de Proprietário.
- CRUD de Livro.
- Consultas por categoria e proprietário.
- Cenários de erro previstos nos testes manuais.
- Persistência de dados após parada e reinicialização dos serviços sem `-v`.
- Execução da API na arquitetura ARM64 da Oracle Cloud.

## Resultado

A Camada 6 está concluída. A API e o banco iniciam na VPS, comunicam-se pela rede interna do Compose, respondem aos endpoints e preservam os dados no volume do PostgreSQL.

---

# Camada 7 - Domínio, proxy reverso e HTTPS

## Status

**Concluída e validada.**

Foram configurados:

- domínio próprio;
- DNS;
- proxy reverso;
- HTTPS;
- acesso público à API;
- bloqueio do acesso externo direto à porta 8080.

## Endereço público

```text
https://api.marcelocarvalho.dev.br/api
```

O subdomínio `api` foi utilizado para a API, preservando o domínio raiz para um possível frontend futuro.

## Arquitetura de produção

```text
Cliente externo
      |
      | HTTPS :443
      v
api.marcelocarvalho.dev.br
      |
      v
Nginx na VPS
      |
      | proxy reverso
      v
Biblioteca API em container
      |
      | rede interna do Docker Compose
      v
PostgreSQL em container
```

## DNS

- Domínio gerenciado no Registro.br.
- DNS configurado para direcionar o subdomínio `api` ao endereço público da VPS.
- Resolução do domínio confirmada durante a implantação.

## Nginx

O Nginx atua como ponto público da aplicação e encaminha as requisições para a API Quarkus.

Funções exercidas:

- atendimento nas portas 80 e 443;
- redirecionamento de HTTP para HTTPS;
- terminação TLS;
- proxy reverso para a API;
- registro de acessos e erros.

## HTTPS

- HTTPS configurado para `api.marcelocarvalho.dev.br`.
- Certificado válido configurado no Nginx.
- Endpoint público testado por HTTPS.

## Validações executadas em 14/10/2026

- Acesso externo direto pela porta 8080 expirou, confirmando que a aplicação não estava acessível externamente por essa porta.
- `GET /api/livros` respondeu `200 OK` pelo domínio HTTPS.
- Três proprietários fictícios foram cadastrados pela API pública.
- Três livros fictícios foram cadastrados após correção do nome do campo `anoDePublicacao` no payload.
- Consultas de proprietários e livros confirmaram os registros persistidos.
- Foram usados apenas dados fictícios.

## Observação funcional identificada

Durante a validação pública, um payload usou `anoPublicacao` em vez de `anoDePublicacao`. O banco recebeu valor nulo para a coluna correspondente e a API retornou erro 500. Após a correção do nome do campo, o cadastro foi concluído.

Esse comportamento deve ser tratado como dívida técnica: payload sem `anoDePublicacao` deve ser rejeitado pela validação da API com resposta 400 antes de alcançar a persistência.

## Segurança atual

- Porta 5432 não exposta publicamente.
- Porta 8080 bloqueada para acesso externo direto.
- Tráfego público destinado à API passa pelo Nginx e por HTTPS.
- SSH permanece na porta 51022 com autenticação por chave.
- A API ainda não possui autenticação e autorização. JWT e autorização por roles permanecem planejados para a Fase 3.

## Resultado

A Camada 7 está concluída: a API está publicada no domínio próprio, responde por HTTPS através do Nginx e não aceita acesso externo direto pela porta 8080.

---

# Configuração atual dos ambientes

## Desenvolvimento local

- PostgreSQL executado pelo Compose local.
- Porta 5432 publicada para a aplicação executada fora do Docker.
- API iniciada por Maven/Quarkus Dev.
- Variáveis de ambiente carregadas no shell antes do Maven.

## Produção

- PostgreSQL e API executados no Docker Compose da VPS.
- PostgreSQL sem porta pública.
- API executada com perfil `prod`.
- Nginx como proxy reverso.
- HTTPS no domínio público.
- Persistência em volume nomeado do Docker.

---

# Operação e verificações

## Containers

```bash
docker compose ps
docker compose logs --tail 100 postgres
docker compose logs --tail 100 biblioteca-api
```

## API interna na VPS

```bash
curl -i http://127.0.0.1:8080/api/proprietarios
curl -i http://127.0.0.1:8080/api/livros
```

## API pública

```bash
curl -i https://api.marcelocarvalho.dev.br/api/proprietarios
curl -i https://api.marcelocarvalho.dev.br/api/livros
```

## Nginx

```bash
sudo nginx -t
sudo systemctl status nginx --no-pager
sudo tail -n 100 /var/log/nginx/biblioteca-api.access.log
sudo tail -n 100 /var/log/nginx/biblioteca-api.error.log
```

## Persistência

Para testar persistência, reiniciar os serviços sem remover volumes:

```bash
docker compose down
docker compose up -d
```

Não utilizar `docker compose down -v` em produção, pois a opção `-v` remove o volume nomeado do banco.

---

# Pendências atuais e próximas fases

As atividades abaixo não impedem a conclusão das Camadas 6 e 7. Elas pertencem à evolução funcional e ao hardening posterior.

## Prioridade imediata

- Desabilitar ou restringir `quarkus.hibernate-orm.log.sql=true` no perfil de produção, agora que a API está publicamente exposta.
- Garantir que a publicação da porta 8080 no Compose fique limitada ao necessário para o Nginx, preferencialmente vinculada ao loopback do host.
- Tratar payload sem `anoDePublicacao` como erro 400 em vez de permitir falha de persistência com 500.
- Confirmar e documentar a renovação automática do certificado HTTPS.

## Fase 2

- Paginação nas listagens.
- Categoria como enum.
- Exceções customizadas e respostas de erro padronizadas.
- Flyway para migrações de banco.
- Troca de `database.generation=update` por `validate` em produção.
- Usuários separados de migração e runtime no PostgreSQL.
- Banco de testes isolado.
- Melhor estrutura de logs.
- Validação algorítmica de CPF.

## Fase 3

- Autenticação JWT.
- Autorização por roles.
- Monitoramento e métricas.
- Cache e otimizações.

## Fase 4

- Frontend em React ou Vue.
- Nginx servindo os arquivos estáticos e mantendo o proxy da API.

---

# Critérios finais confirmados

## Camada 6

- [x] API empacotada em imagem compatível com ARM64.
- [x] Java 21 no build e no runtime.
- [x] PostgreSQL e API executados em containers.
- [x] API conecta ao banco pelo serviço `postgres`.
- [x] Endpoints validados.
- [x] Persistência validada após reinicialização.
- [x] Execução validada na Oracle Cloud VPS.

## Camada 7

- [x] Domínio configurado.
- [x] DNS apontando para a VPS.
- [x] Nginx configurado como proxy reverso.
- [x] HTTPS configurado.
- [x] API acessível em `api.marcelocarvalho.dev.br`.
- [x] Porta 8080 inacessível externamente.
- [x] Leituras e escritas testadas pela URL pública.
- [x] Persistência confirmada.

---

# Conclusão

O ciclo de infraestrutura planejado para a versão inicial está concluído. A Biblioteca API está compilada com Java 21, executa em container ARM64 na Oracle Cloud, utiliza PostgreSQL persistente em container e está publicada por domínio próprio através do Nginx com HTTPS.

As próximas atividades deixam de ser implantação básica e passam a ser evolução de produto, qualidade, observabilidade, migração de banco e segurança da API.

[Voltar ao README](../README.md)
