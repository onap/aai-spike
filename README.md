# Spike  Microservice

The Spike microservice consumes and processes the Champ graph data event stream from which it generates events to be consumed by other components.
 
---

## Public Interfaces

### Stream output

Spike receives events from the Champ microservice regarding changes to the graph database. Spike will buffer these events in an attempt to ensure proper chronological ordering, and then output them onto a configurable kafka or DMaaP queue.

The messages are in a similar format to Gizmo's async pipeline. Here are some examples:

#### Vertex
##### Create Vertex

    {
      "transaction-id": "38fe6bb0-6b3b-4c1d-95ea-7a9f5a29d857",
      "vertex": {
        "schema-version": "v11",
        "type": "vserver",
        "key": "a7cbd3fb-a7ee-4fec-91fb-d94793b4c641",
        "properties": {
          "in-maint": false,
          "vserver-name": "dan",
          "vserver-id": "dan-vserv",
          "last-mod-source-of-truth": "Dan-laptop",
          "source-of-truth": "Dan-laptop",
          "vserver-selflink": "something",
          "is-closed-loop-disabled": false
        }
      },
      "operation": "CREATE",
      "timestamp": 1515524454947
    }
    
##### Update Vertex

    {
      "transaction-id": "cf0ef29b-3f38-42d9-8e65-2e03a8c97aae",
      "vertex": {
        "schema-version": "v11",
        "type": "vserver",
        "key": "a7cbd3fb-a7ee-4fec-91fb-d94793b4c641",
        "properties": {
          "in-maint": false,
          "vserver-name": "dan-updated",
          "vserver-id": "dan-vserv",
          "last-mod-source-of-truth": "Dan-laptop",
          "vserver-selflink": "something",
          "is-closed-loop-disabled": false
        }
      },
      "operation": "UPDATE",
      "timestamp": 1515525164176
    }
    
##### Delete Vertex

    {
      "transaction-id": "3962b400-f5b2-4159-a0eb-cb7aa4e48580",
      "vertex": {
        "schema-version": "v11",
        "type": "vserver",
        "key": "a7cbd3fb-a7ee-4fec-91fb-d94793b4c641",
        "properties": {
          "in-maint": false,
          "vserver-name": "dan-updated",
          "vserver-id": "dan-vserv",
          "last-mod-source-of-truth": "Dan-laptop",
          "vserver-selflink": "something",
          "is-closed-loop-disabled": false
        }
      },
      "operation": "DELETE",
      "timestamp": 1515525766057
    }
    
#### Relationship
##### Create Relationship

    {
      "transaction-id": "e654eee5-d8fc-445e-b09d-7dba20029a78",
      "relationship": {
        "source": {
          "type": "vserver",
          "key": "2223dc8b-9e26-4c90-b767-7d1f61e8fa8d"
        },
        "schema-version": "v12",
        "type": "tosca.relationships.HostedOn",
        "key": "6a443848-21bb-4801-ab00-53ee286c290d",
        "properties": {
          "prevent-delete": "java.lang.String",
          "SVC-INFRA": "java.lang.String",
          "delete-other-v": "java.lang.String",
          "contains-other-v": "java.lang.String"
        },
        "target": {
          "type": "pserver",
          "key": "32ed5257-a6ac-4d63-8635-ae1d3a615960"
        }
      },
      "operation": "CREATE",
      "timestamp": 1515526740371
    }
    
##### Update Relationship

    {
      "transaction-id": "db8d1626-346d-4f04-8856-f5a5e0e54313",
      "relationship": {
        "source": {
          "type": "vserver",
          "key": "2223dc8b-9e26-4c90-b767-7d1f61e8fa8d"
        },
        "schema-version": "v12",
        "type": "tosca.relationships.HostedOn",
        "key": "6a443848-21bb-4801-ab00-53ee286c290d",
        "properties": {
          "prevent-delete": "java.lang.String",
          "SVC-INFRA": "java.lang.String",
          "delete-other-v": "updated",
          "contains-other-v": "java.lang.String"
        },
        "target": {
          "type": "pserver",
          "key": "32ed5257-a6ac-4d63-8635-ae1d3a615960"
        }
      },
      "operation": "UPDATE",
      "timestamp": 1515526920973
    }
    
##### Delete Relationship

    {
      "transaction-id": "e7dbd137-cc07-41c4-8ba4-694334f4b2e4",
      "relationship": {
        "source": {
          "type": "vserver",
          "key": "2223dc8b-9e26-4c90-b767-7d1f61e8fa8d"
        },
        "schema-version": "v12",
        "type": "tosca.relationships.HostedOn",
        "key": "6a443848-21bb-4801-ab00-53ee286c290d",
        "properties": {
          "prevent-delete": "java.lang.String",
          "SVC-INFRA": "java.lang.String",
          "delete-other-v": "updated",
          "contains-other-v": "java.lang.String"
        },
        "target": {
          "type": "pserver",
          "key": "32ed5257-a6ac-4d63-8635-ae1d3a615960"
        }
      },
      "operation": "DELETE",
      "timestamp": 1515527840630
    }
    
#### Transactions

Champ, and therefore Spike, will publish separate events for each operation done within a transaction. Operations from the same transaction can be identified via the database-transaction-id field.

##### Transaction example

The following three events were created in a single bulk request from Gizmo. Note the database-transaction-id.

    {
      "transaction-id": "7397ce4c-70ed-4b32-b8d0-24b6496e1791",
      "vertex": {
        "schema-version": "v11",
        "type": "vserver",
        "key": "b7c80b13-3b32-4007-83c6-553617d64cfa",
        "properties": {
          "in-maint": false,
          "vserver-name": "dan-vserver-1",
          "prov-status": "Provisioned",
          "vserver-id": "Vserver-AMT-002-HSGW",
          "last-mod-source-of-truth": "Dan-laptop",
          "vserver-name2": "Vs2-HSGW-OTT",
          "source-of-truth": "Dan-laptop",
          "vserver-selflink": "AMT VserverLink",
          "is-closed-loop-disabled": false
        }
      },
      "database-transaction-id": "52eb5657-0d43-4e4b-a4d5-042acc9bc574",
      "operation": "CREATE",
      "timestamp": 1515528272536
    }
    
    {
      "transaction-id": "ec3d0552-e20b-4fc1-aa8e-634ddd2a9d76",
      "vertex": {
        "schema-version": "v11",
        "type": "pserver",
        "key": "ae43af1c-8479-4358-9325-416d3a854d69",
        "properties": {
          "ptnii-equip-name": "amdocs199snd9",
          "hostname": "dan-bulk-1",
          "equip-type": "server",
          "equip-vendor": "HP",
          "equip-model": "DL380p-nd",
          "in-maint": false,
          "fqdn": "amdocs199snd9.amdocs.lab.com",
          "purpose": "",
          "resource-version": "1477013499",
          "ipv4-oam-address": "135.182.138.60",
          "last-mod-source-of-truth": "Dan-laptop",
          "source-of-truth": "Dan-laptop"
        }
      },
      "database-transaction-id": "52eb5657-0d43-4e4b-a4d5-042acc9bc574",
      "operation": "CREATE",
      "timestamp": 1515528272841
    }
    
    {
      "transaction-id": "81b6a5bc-bf82-4043-92a7-f3bafe647f8e",
      "database-transaction-id": "52eb5657-0d43-4e4b-a4d5-042acc9bc574",
      "relationship": {
        "source": {
          "type": "vserver",
          "key": "b7c80b13-3b32-4007-83c6-553617d64cfa"
        },
        "schema-version": "v12",
        "type": "tosca.relationships.HostedOn",
        "key": "592d93d5-a17d-4dfd-83f2-68b777da0481",
        "properties": {
          "prevent-delete": "asdf",
          "SVC-INFRA": "fdsa",
          "delete-other-v": "asdf",
          "contains-other-v": "fdsa"
        },
        "target": {
          "type": "pserver",
          "key": "ae43af1c-8479-4358-9325-416d3a854d69"
        }
      },
      "operation": "CREATE",
      "timestamp": 1515528273738
    }

### Echo Service
The Spike micro service supports the standard echo service to allow it to be 'pinged' to verify that the service is up and responding.

The echo service is reachable via the following REST end point:

    https://<host>:9518/services/spike/v1/echo-service/echo
