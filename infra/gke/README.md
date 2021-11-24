# GKE cluster via Pulumi

This is the first project using Pulumi for deploying a GKE cluster and a Serverless VPC connector.
It also takes care of the IAM of both resources. It deploys it all in `moneycol` GCP project and requires
prior authentication into this project (it is not CI ready).

It deploys the following:

- A VPC-native GKE cluster with Config Connector and Workload Identity
- A Service Account for the nodes of the cluster
- A Custom IAM Role for the SA and Binding
- A `kubeconfig` to be able to connect to the cluster if needed to deploy some base resources
- A Serverless VPC connector into the default network

## To execute

- For now it's only local, and `pulumi` should be installed in the path and NodeJS v14 selected:

```
nvm use v14
node -v
pulumi version
```

- Install Stack with change visualization/prompt:

```
pulumi up --diff
```

- Select stack

```
                                                                                                              
Please choose a stack, or create a new one:  [Use arrows to move, enter to select, type to filter]
> dev2
  <create a new stack>
```


- Check the changes
```
Previewing update (dev2)

View Live: https://app.pulumi.com/davidfernandezm/moneycol-gke/dev2/previews/f3247e24-d565-4a77-a0a3-c552aafab893

  pulumi:pulumi:Stack: (same)
    [urn=urn:pulumi:dev2::moneycol-gke::pulumi:pulumi:Stack::moneycol-gke-dev2]
Resources:
    8 unchanged
Do you want to perform this update? details
  pulumi:pulumi:Stack: (same)
    [urn=urn:pulumi:dev2::moneycol-gke::pulumi:pulumi:Stack::moneycol-gke-dev2]

Do you want to perform this update?  [Use arrows to move, enter to select, type to filter]
> yes
  no
  details
```

- Accept or deny the update

- Accept or deny the update

## Delete values from Pulumi state

There's times that some resources need to be changed manually in GCP Console and Pulumi wouln't notice. In order for that to work, it's needed to delete the URN of the manually changed resource from Pulumi's state:

```
pulumi stack --show-urns

Please choose a stack, or create a new one: dev2
Current stack is dev2:
    Owner: davidfernandezm
    Last updated: 2 minutes ago (2021-11-24 00:04:22.368422 +0000 UTC)
    Pulumi version: v3.16.0
Current stack resources (9):
    TYPE                                         NAME
    pulumi:pulumi:Stack                          moneycol-gke-dev2
    │  URN: urn:pulumi:dev2::moneycol-gke::pulumi:pulumi:Stack::moneycol-gke-dev2
    ├─ gcp:vpcaccess/connector:Connector         moneycolvpcconnectordev
    │     URN: urn:pulumi:dev2::moneycol-gke::gcp:vpcaccess/connector:Connector::moneycolvpcconnectordev
    ├─ gcp:projects/iAMCustomRole:IAMCustomRole  gke_nodes_role
    │     URN: urn:pulumi:dev2::moneycol-gke::gcp:projects/iAMCustomRole:IAMCustomRole::gke_nodes_role
    ├─ gcp:serviceAccount/account:Account        gke-nodes-moneycol
    │     URN: urn:pulumi:dev2::moneycol-gke::gcp:serviceAccount/account:Account::gke-nodes-moneycol
    ├─ gcp:projects/iAMBinding:IAMBinding        project
    │     URN: urn:pulumi:dev2::moneycol-gke::gcp:projects/iAMBinding:IAMBinding::project
    ├─ gcp:container/cluster:Cluster             cluster-dev2
    │     URN: urn:pulumi:dev2::moneycol-gke::gcp:container/cluster:Cluster::cluster-dev2
    ├─ gcp:container/nodePool:NodePool           elasticsearch-pool
    │     URN: urn:pulumi:dev2::moneycol-gke::gcp:container/nodePool:NodePool::elasticsearch-pool
    ├─ pulumi:providers:kubernetes               cluster-dev2
    │     URN: urn:pulumi:dev2::moneycol-gke::pulumi:providers:kubernetes::cluster-dev2
    └─ pulumi:providers:gcp                      default_5_21_0
          URN: urn:pulumi:dev2::moneycol-gke::pulumi:providers:gcp::default_5_21_0
```

After choosing one from the list, then issue a `delete` command on the URN:

```
pulumi state delete urn:pulumi:dev2::moneycol-gke::gcp:container/cluster:Cluster::cluster-dev2 --force
```

The selected resource may have dependencies on others, those will need to be deleted first:

```
pulumi state delete urn:pulumi:dev2::moneycol-gke::pulumi:providers:kubernetes::cluster-dev2
```

There can also be protections in place, those could be overcome with --force
