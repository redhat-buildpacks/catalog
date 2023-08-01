## RHTAP Templates

This project contains the code source needed to feed a RHTAP runtime demo project like also Tekton PipelineAsCode

To use them, perform the following actions using the github cli:

- Git auth
`echo $(PASSWORD_STORE_DIR=~/.password-store-work pass show github/apps/dabou-1/github_token) | gh auth login --with-token`

- Create github project

```bash
ORG_NAME=halkyonio
REPO_TEMPLATE=rhtap-templates
REPO_DEMO=rhtap-demo1

rm -rf $REPO_NAME; mkdir $REPO_NAME; cd $REPO_NAME
git init
echo "## RHTAP Demo 1" > README.md
git add .

## Import runtime code
BRANCH=main
wget https://github.com/$ORG_NAME/$REPO_TEMPLATE/archive/$BRANCH.zip
unzip $BRANCH.zip; rm $BRANCH.zip
mv $REPO_TEMPLATE/quarkus-hello .
rm -r $REPO_TEMPLATE-$BRANCH
git add .
git commit -m "Upload quarkus hello runtime"
#gh repo create $ORG_NAME/$REPO_NAME --public --add-readme --source . --push
```



