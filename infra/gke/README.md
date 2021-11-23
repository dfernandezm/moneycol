# GKE cluster via Pulumi

This is the first project using Pulumi for deploying a GKE cluster and a Serverless VPC connector.
It also takes care of the IAM of both resources. It deploys it all in `moneycol` GCP project and requires
prior authentication into this project (it is not CI ready).

It deploys the following:

- A VPC-native GKE cluster with Config Connector and Workload Identity
- A Service Account for the nodes of the cluster
- A Custom IAM Role for the SA and Binding
- A `kubeconfig` to be able to connect to the cluster if needed to deploy some base resources
- A Serverless VPC connector into the default network

## To execute

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