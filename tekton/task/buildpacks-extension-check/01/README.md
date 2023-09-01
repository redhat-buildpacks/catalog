# `buildpacks extension check`

**Note: this Task is only compatible with Tekton Pipelines versions 0.45.0 and greater!**

This `Task` will inspect a Buildpacks builder image using the skopeo tool
to find if the image includes the labels: `io.buildpacks.extension.layers` and `io.buildpacks.buildpack.order-extensions`.
If this is the case, then, you can use the `results.extensionLabels` within your PipelineRun or TaskRun to
trigger a build using either the `buildpacks extension` Task or the `buildpacks` Task.
Additionally, the CNB USER ID and CNB GROUP ID of the image will be exported as `results`: uid and gid.


## Install the task

```bash
kubectl apply -f https://raw.githubusercontent.com/redhat-buildpacks/templates/main/tekton/task/buildpacks-extension-check/01/buildpacks-extension-check.yaml
```

## Parameters

* **url**: Repository URL to clone from. (_required_)
* **verbose**: Log the commands that are executed during `git-clone`'s operation. (_default_: true)
* **userHome**: The user's home directory. (_default_: "/tekton/home")

## Results

* **commit**: The precise commit SHA that was fetched by this Task
* **url**: The precise URL that was fetched by this Task
* **committer-date**: The epoch timestamp of the commit that was fetched by this Task

## Platforms

The Task can be run on `linux/amd64` platforms.

## Usage

### PipelineRun

Create a PipelineRun and pass as prameter the buildpacks builder image to inspect

```bash
cat <<'EOF' | kubectl create -f -
---
apiVersion: tekton.dev/v1beta1
kind: PipelineRun
metadata:
  name: check-builder-image
spec:
  params:
    - name: CNB_BUILDER_IMAGE
      value: paketocommunity/builder-ubi-base
  pipelineSpec:
    params:
      - name: CNB_BUILDER_IMAGE
        type: string
    tasks:
      - name: buildpacks-extension-check
        params:
        - name: builderImage
          value: $(params.CNB_BUILDER_IMAGE)
        taskRef:
          resolver: git
          params:
            - name: url
              value: https://github.com/redhat-buildpacks/templates.git
            - name: revision
              value: main
            - name: pathInRepo
              value: tekton/task/buildpacks-extension-check/01/buildpacks-extension-check.yaml
      - name: show-uid
        # Param needed to interpolate the results !!
        params:
        - name: uid
          value: $(tasks.buildpacks-extension-check.results.uid)
        - name: gid
          value: $(tasks.buildpacks-extension-check.results.gid)
        - name: extensionLabels
          value: $(tasks.buildpacks-extension-check.results.extensionLabels)
        taskSpec:
          steps:
            - name: show-uid-gid
              image: quay.io/swsmirror/bash
              script: |
                #!/usr/bin/env bash
                set -e

                echo "Extension labels: $(params.extensionLabels)"
                echo "CNB user id : $(params.uid)"
                echo "CNB group id: $(params.gid)"
---
EOF

tkn pr logs check-builder-image -f
```

### Registry authentication

To bypass the docker limit rate or to be logged with the container image registry, it is needed to mount your `auths.json` file containing the credentials in a kubernetes secret. This secret will be passed as parameter to the ServiceAccount. To let the TaskRun/PipelineRun to use it, set the `serviceAccount` parameter with the name of the serviceAccount.
When Tekton will run the task's pod, then it will mount the `.dockerconfigjson` under the following path: `/tekton/home/creds-secrets/<name_of_the_kubernetes_secret>/.dockerconfigjson`.

Example:
```bash
kubectl create secret docker-registry dockercfg \
  --docker-server="https://index.docker.io/v1/" \
  --docker-username="<REGISTRY_USERNAME>" \
  --docker-password="<REGISTRY_PASSWORD>"

cat <<EOF | kubectl apply -f -
---  
apiVersion: v1
imagePullSecrets:
- name: dockercfg
kind: ServiceAccount
metadata:
  name: sa-with-secret
secrets:
- name: dockercfg
---
apiVersion: tekton.dev/v1beta1
kind: PipelineRun
metadata:
  name: buildpacks-phases
spec:
  serviceAccountName: sa-with-secret
...  
```