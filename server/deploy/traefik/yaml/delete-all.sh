kubectl -n kubesystem delete clusterrole traefik-ingress-controller
kubectl -n kubesystem delete traefik-ingress-controller
kubectl -n kubesystem delete clusterrolebinding traefik-ingress-controller
kubectl -n kubesystem delete sa traefik-ingress-controller
kubectl delete sa traefik-ingress-controller -n kube-system