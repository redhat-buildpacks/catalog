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

gh repo delete $ORG_NAME/$REPO_DEMO --yes
gh repo create $ORG_NAME/$REPO_DEMO --public

rm -rf $REPO_NAME; mkdir $REPO_NAME; cd $REPO_NAME
git init
echo "## RHTAP Demo 1" > README.md
echo "$REPO_TEMPLATE-$BRANCH/" > .gitignore

git add .

## Import runtime code
BRANCH=main
wget https://github.com/$ORG_NAME/$REPO_TEMPLATE/archive/$BRANCH.zip
unzip $BRANCH.zip
mv $REPO_TEMPLATE-$BRANCH/quarkus-hello/.mvn/ .
mv $REPO_TEMPLATE-$BRANCH/quarkus-hello/* .

git add .
git commit -m "Upload quarkus hello runtime"

git branch -M main
git remote add origin git@github.com:$ORG_NAME/$REPO_DEMO.git
git push -u origin main
cd ..
```
- Test it locally
```bash
http :8080/hello
http :8080/hello/greeting/charles
```
- Creating a .Tekton project
```bash
cd $REPO_NAME
mkdir .tekton
mv $REPO_TEMPLATE-$BRANCH/tekton/template-push.yaml .tekton/$REPO_NAME-push.yaml
cd ..
```

- Cleaning
```bash
rm $BRANCH.zip; rm -r $REPO_TEMPLATE-$BRANCH
```



