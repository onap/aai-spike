# Spike  Microservice

The Spike microservice consumes and processes the Champ graph data event stream from which it generates events to be consumed by other components.
 
## Public Interfaces

### Stream output

Spike receives events from the Champ microservice regarding changes to the graph database. Spike will buffer these events in an attempt to ensure proper chronological ordering, and then output them onto a configurable kafka or DMaaP queue.

The messages are in a similar format to Gizmo's async pipeline. Here are some examples:

#### Vertex
##### Create Vertex

   {  
      "header":{  
         "event-type":"update-notification",
         "request-id":"8f9931a2-8002-4cb4-917b-a8c984932021",
         "source-name":"SPIKE",
         "timestamp":"20180807T153514Z"
      },
      "body":{  
         "transaction-id":"8f9931a2-8002-4cb4-917b-a8c984932021",
         "vertex":{  
            "schema-version":"V14",
            "type":"vserver",
            "key":"541cf447-09f5-4484-a765-845e71aab1f2",
            "properties":{  
               "aai-last-mod-ts":1533656090789,
               "in-maint":false,
               "aai-uuid":"541cf447-09f5-4484-a765-845e71aab1f2",
               "vserver-name":"test-vserver",
               "aai-created-ts":1533656090789,
               "vserver-id":"VSER1",
               "last-mod-source-of-truth":"Gizmo-Creator",
               "vserver-name2":"alt-test-vserver",
               "source-of-truth":"Gizmo-Creator",
               "vserver-selflink":"http://1.2.3.4/moreInfo",
               "is-closed-loop-disabled":false,
               "aai-node-type":"vserver"
            }
         },
         "operation":"CREATE",
         "timestamp":1533656091474
      }
   }
    
##### Update Vertex

   {  
      "header":{  
         "event-type":"update-notification",
         "request-id":"8f9931a2-8002-4cb4-917b-a8c984932021",
         "source-name":"SPIKE",
         "timestamp":"20180807T153514Z"
      },
      "body":{  
         "transaction-id":"8f9931a2-8002-4cb4-917b-a8c984932021",
         "vertex":{  
            "schema-version":"V14",
            "type":"vserver",
            "key":"541cf447-09f5-4484-a765-845e71aab1f2",
            "properties":{  
               "aai-last-mod-ts":1533656090789,
               "in-maint":false,
               "aai-uuid":"541cf447-09f5-4484-a765-845e71aab1f2",
               "vserver-name":"test-vserver",
               "aai-created-ts":1533656090789,
               "vserver-id":"VSER1",
               "last-mod-source-of-truth":"Gizmo-Creator",
               "vserver-name2":"alt-test-vserver",
               "source-of-truth":"Gizmo-Creator",
               "vserver-selflink":"http://1.2.3.4/moreInfo",
               "is-closed-loop-disabled":false,
               "aai-node-type":"vserver"
            }
         },
         "operation":"UPDATE",
         "timestamp":1533656091474
      }
   }
    
##### Delete Vertex

   {  
      "header":{  
         "event-type":"update-notification",
         "request-id":"8f9931a2-8002-4cb4-917b-a8c984932021",
         "source-name":"SPIKE",
         "timestamp":"20180807T153514Z"
      },
      "body":{  
         "transaction-id":"8f9931a2-8002-4cb4-917b-a8c984932021",
         "vertex":{  
            "schema-version":"V14",
            "type":"vserver",
            "key":"541cf447-09f5-4484-a765-845e71aab1f2",
            "properties":{  
               "aai-last-mod-ts":1533656090789,
               "in-maint":false,
               "aai-uuid":"541cf447-09f5-4484-a765-845e71aab1f2",
               "vserver-name":"test-vserver",
               "aai-created-ts":1533656090789,
               "vserver-id":"VSER1",
               "last-mod-source-of-truth":"Gizmo-Creator",
               "vserver-name2":"alt-test-vserver",
               "source-of-truth":"Gizmo-Creator",
               "vserver-selflink":"http://1.2.3.4/moreInfo",
               "is-closed-loop-disabled":false,
               "aai-node-type":"vserver"
            }
         },
         "operation":"DELETE",
         "timestamp":1533656091474
      }
   }
    
#### Relationship
##### Create Relationship

   {  
      "header":{  
         "event-type":"update-notification",
         "request-id":"1f2eced7-8300-4e72-966d-f345027c987a",
         "source-name":"SPIKE",
         "timestamp":"20180807T153514Z"
      },
      "body":{  
         "transaction-id":"1f2eced7-8300-4e72-966d-f345027c987a",
         "relationship":{  
            "source":{  
               "type":"vserver",
               "key":"ed76551f-f47f-47f9-a23d-052634446e76"
            },
            "schema-version":"v14",
            "type":"tosca.relationships.HostedOn",
            "key":"cd5b98fd-9028-4211-af02-0bc839f9a47b",
            "properties":{  
               "prevent-delete":"IN",
               "SVC-INFRA":"OUT",
               "delete-other-v":"NONE",
               "contains-other-v":"NONE"
            },
            "target":{  
               "type":"pserver",
               "key":"981c0494-c742-4d75-851c-8194bbbd8a96"
            }
         },
         "operation":"CREATE",
         "timestamp":1533656086207
      }
   }
    
##### Update Relationship

   {  
      "header":{  
         "event-type":"update-notification",
         "request-id":"1f2eced7-8300-4e72-966d-f345027c987a",
         "source-name":"SPIKE",
         "timestamp":"20180807T153514Z"
      },
      "body":{  
         "transaction-id":"1f2eced7-8300-4e72-966d-f345027c987a",
         "relationship":{  
            "source":{  
               "type":"vserver",
               "key":"ed76551f-f47f-47f9-a23d-052634446e76"
            },
            "schema-version":"v14",
            "type":"tosca.relationships.HostedOn",
            "key":"cd5b98fd-9028-4211-af02-0bc839f9a47b",
            "properties":{  
               "prevent-delete":"IN",
               "SVC-INFRA":"OUT",
               "delete-other-v":"NONE",
               "contains-other-v":"NONE"
            },
            "target":{  
               "type":"pserver",
               "key":"981c0494-c742-4d75-851c-8194bbbd8a96"
            }
         },
         "operation":"UPDATE",
         "timestamp":1533656086207
      }
   }
    
##### Delete Relationship

   {  
      "header":{  
         "event-type":"update-notification",
         "request-id":"1f2eced7-8300-4e72-966d-f345027c987a",
         "source-name":"SPIKE",
         "timestamp":"20180807T153514Z"
      },
      "body":{  
         "transaction-id":"1f2eced7-8300-4e72-966d-f345027c987a",
         "relationship":{  
            "source":{  
               "type":"vserver",
               "key":"ed76551f-f47f-47f9-a23d-052634446e76"
            },
            "schema-version":"v14",
            "type":"tosca.relationships.HostedOn",
            "key":"cd5b98fd-9028-4211-af02-0bc839f9a47b",
            "properties":{  
               "prevent-delete":"IN",
               "SVC-INFRA":"OUT",
               "delete-other-v":"NONE",
               "contains-other-v":"NONE"
            },
            "target":{  
               "type":"pserver",
               "key":"981c0494-c742-4d75-851c-8194bbbd8a96"
            }
         },
         "operation":"DELETE",
         "timestamp":1533656086207
      }
   }
    
#### Transactions

Champ, and therefore Spike, will publish separate events for each operation done within a transaction. Operations from the same transaction can be identified via the database-transaction-id field.

##### Transaction example

The following three events were created in a single bulk request from Gizmo. Note the database-transaction-id.

[  
   {  
      "header":{  
         "event-type":"update-notification",
         "request-id":"9fc953af-bb41-4cfe-b522-dc6a9d1b5830",
         "source-name":"SPIKE",
         "timestamp":"20180807T162714Z"
      },
      "body":{  
         "transaction-id":"9fc953af-bb41-4cfe-b522-dc6a9d1b5830",
         "vertex":{  
            "schema-version":"V14",
            "type":"vserver",
            "key":"9c649f2b-7500-4b35-abb8-d51008fb28fe",
            "properties":{  
               "aai-last-mod-ts":1533659209321,
               "in-maint":false,
               "aai-uuid":"9c649f2b-7500-4b35-abb8-d51008fb28fe",
               "vserver-name":"test-vserver",
               "aai-created-ts":1533659209321,
               "vserver-id":"VSER1",
               "last-mod-source-of-truth":"test1",
               "vserver-name2":"alt-test-vserver",
               "source-of-truth":"test1",
               "vserver-selflink":"http://1.2.3.4/moreInfo",
               "is-closed-loop-disabled":false
            }
         },
         "database-transaction-id":"d8607c70-e5d6-44d4-bc6e-d8f7af419378",
         "operation":"CREATE",
         "timestamp":1533659209324
      }
   },
   {  
      "header":{  
         "event-type":"update-notification",
         "request-id":"6f7db331-a0b9-4194-917c-7f24124b5d46",
         "source-name":"SPIKE",
         "timestamp":"20180807T162714Z"
      },
      "body":{  
         "transaction-id":"6f7db331-a0b9-4194-917c-7f24124b5d46",
         "vertex":{  
            "schema-version":"V14",
            "type":"pserver",
            "key":"ecec281a-c0c0-47af-8ec8-019753edc28c",
            "properties":{  
               "ptnii-equip-name":"e-name",
               "aai-last-mod-ts":1533659209637,
               "equip-type":"server",
               "equip-vendor":"HP",
               "fqdn":"myhost.onap.net",
               "purpose":"my-purpose",
               "aai-created-ts":1533659209637,
               "ipv4-oam-address":"1.2.3.4",
               "source-of-truth":"test1",
               "hostname":"steve-host2",
               "equip-model":"DL380p-nd",
               "in-maint":false,
               "aai-uuid":"ecec281a-c0c0-47af-8ec8-019753edc28c",
               "last-mod-source-of-truth":"test1"
            }
         },
         "database-transaction-id":"d8607c70-e5d6-44d4-bc6e-d8f7af419378",
         "operation":"CREATE",
         "timestamp":1533659209645
      }
   },
   {  
      "header":{  
         "event-type":"update-notification",
         "request-id":"54f0f573-eb22-4ce2-b51b-4af0904c9782",
         "source-name":"SPIKE",
         "timestamp":"20180807T162714Z"
      },
      "body":{  
         "transaction-id":"54f0f573-eb22-4ce2-b51b-4af0904c9782",
         "database-transaction-id":"d8607c70-e5d6-44d4-bc6e-d8f7af419378",
         "relationship":{  
            "source":{  
               "type":"vserver",
               "key":"9c649f2b-7500-4b35-abb8-d51008fb28fe"
            },
            "schema-version":"v14",
            "type":"tosca.relationships.HostedOn",
            "key":"410cb65a-1b46-4d23-a5b4-9f57bdc918c1",
            "properties":{  
               "prevent-delete":"IN",
               "SVC-INFRA":"OUT",
               "delete-other-v":"NONE",
               "contains-other-v":"NONE"
            },
            "target":{  
               "type":"pserver",
               "key":"ecec281a-c0c0-47af-8ec8-019753edc28c"
            }
         },
         "operation":"CREATE",
         "timestamp":1533659210721
      }
   }
]

### Echo Service
The Spike micro service supports the standard echo service to allow it to be 'pinged' to verify that the service is up and responding.

The echo service is reachable via the following REST end point:

    https://<host>:9518/services/spike/v1/echo-service/echo
