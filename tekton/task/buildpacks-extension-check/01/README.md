# `git-clone`

**Note: this Task is only compatible with Tekton Pipelines versions 0.45.0 and greater!**

This `Task` has two required inputs:

1. The CNB Builder image to be inspected by the skopeo tool using the `cnb_xxx` param.
2. A Workspace called `output`.

The `git-clone` `Task` will clone a repo from the provided `url` into the
`output` Workspace. By default the repo will be cloned into the root of
your Workspace. You can clone into a subdirectory by setting this `Task`'s
`subdirectory` param. If the directory where the repo will be cloned is
already populated then by default the contents will be deleted before the
clone takes place. This behaviour can be disabled by setting the
`deleteExisting` param to `"false"`.

**Note**: The `git-clone` Task is run as nonroot. The files cloned on to the `output`
workspace will end up owned by user 65532.

## Workspaces

**Note**: This task is run as a non-root user with UID 65532 and GID 65532.
Generally, the default permissions for storage volumes are configured for the
root user. To make the volumes accessible by the non-root user, you will need
to either configure the permissions manually or set the `fsGroup` field under
`PodSecurityContext` in your TaskRun or PipelineRun.

An example PipelineRun will look like:
```yaml
apiVersion: tekton.dev/v1beta1
kind: PipelineRun
metadata:
  generateName: git-clone-
spec:
  pipelineRef:
    name: git-clone-pipeline
  podTemplate:
    securityContext:
      fsGroup: 65532
...
...
```

An example TaskRun will look like:
```yaml
apiVersion: tekton.dev/v1beta1
kind: TaskRun
metadata:
  name: taskrun
spec:
  taskRef:
    name: git-clone
  podTemplate:
    securityContext:
      fsGroup: 65532
...
...
```

* **output**: A workspace for this Task to fetch the git repository in to.
* **ssh-directory**: An optional workspace to provide SSH credentials. At
  minimum this should include a private key but can also include other common
  files from `.ssh` including `config` and `known_hosts`. It is **strongly**
  recommended that this workspace be bound to a Kubernetes `Secret`.

* **ssl-ca-directory**: An optional workspace to provide custom CA certificates.
  Like the /etc/ssl/certs path this directory can have any pem or cert files,
  this uses libcurl ssl capath directive. See this SO answer here
  https://stackoverflow.com/a/9880236 on how it works.

* **basic-auth**: An optional workspace containing `.gitconfig` and
  `.git-credentials` files. This allows username/password/access token to be
  provided for basic auth.

  It is **strongly** recommended that this workspace be bound to a Kubernetes
  `Secret`. For details on the correct format of the files in this Workspace
  see [Using basic-auth Credentials](#using-basic-auth-credentials) below.

  **Note**: Settings provided as part of a `.gitconfig` file can affect the
  execution of `git` in ways that conflict with the parameters of this Task.
  For example, specifying proxy settings in `.gitconfig` could conflict with
  the `httpProxy` and `httpsProxy` parameters this Task provides. Nothing
  prevents you setting these parameters but it is not advised.

## Parameters

* **url**: Repository URL to clone from. (_required_)
* **revision**: Revision to checkout. (branch, tag, sha, ref, etc...) (_default_: "")
* **refspec**: Refspec to fetch before checking out revision. (_default_:"")
* **submodules**: Initialize and fetch git submodules. (_default_: true)
* **depth**: Perform a shallow clone, fetching only the most recent N commits. (_default_: 1)
* **sslVerify**: Set the `http.sslVerify` global git config. Setting this to `false` is not advised unless you are sure that you trust your git remote. (_default_: true)
* **crtFileName**: If `sslVerify` is **true** and `ssl-ca-directory` workspace is given then set `crtFileName` if mounted file name is different than `ca-bundle.crt`. (_default_: "ca-bundle.crt")
* **subdirectory**: Subdirectory inside the `output` workspace to clone the repo into. (_default:_ "")
* **deleteExisting**: Clean out the contents of the destination directory if it already exists before cloning. (_default_: true)
* **httpProxy**: HTTP proxy server for non-SSL requests. (_default_: "")
* **httpsProxy**: HTTPS proxy server for SSL requests. (_default_: "")
* **noProxy**: Opt out of proxying HTTP/HTTPS requests. (_default_: "")
* **verbose**: Log the commands that are executed during `git-clone`'s operation. (_default_: true)
* **sparseCheckoutDirectories**: Which directories to match or exclude when performing a sparse checkout (_default_: "")
* **gitInitImage**: The image providing the git-init binary that this Task runs. (_default_: "gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/git-init:TODO")
* **userHome**: The user's home directory. (_default_: "/tekton/home")

## Results

* **commit**: The precise commit SHA that was fetched by this Task
* **url**: The precise URL that was fetched by this Task
* **committer-date**: The epoch timestamp of the commit that was fetched by this Task

## Platforms

The Task can be run on `linux/amd64`, `linux/s390x`, `linux/arm64`, and `linux/ppc64le` platforms.

## Usage