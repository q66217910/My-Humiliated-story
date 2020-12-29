## 1.防火墙

1. 关闭防火墙

   ```
   systemctl stop firewalld
   ```

2.  关闭selinux 

   ```
   setenforce 0
   ```

3.  关闭swapoff 

   ```
   swapoff -a   
   ```

4.  将桥接的IPV4流量传递到iptables 的链 

   ```
   cat > /etc/sysctl.d/k8s.conf << EOF
   net.bridge.bridge-nf-call-ip6tables = 1
   net.bridge.bridge-nf-call-iptables = 1
   EOF
   ```

5. sysctl --system

   ```
   sysctl --system
   ```



## 2.安装Docker

1.  配置yum源 

   ```
   wget https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo -O /etc/yum.repos.d/docker-ce.repo
   ```

2. 安装docker

   ```
   yum -y install docker-ce-18.06.1.ce-3.el7
   ```

3. 配置镜像加速

   ```
   tee /etc/docker/daemon.json <<-'EOF'
   {
     "registry-mirrors": ["https://jnboye7q.mirror.aliyuncs.com"]
   }
   EOF
   ```

4. 启动docker

   ```
   systemctl enable docker
   systemctl start docker
   ```



## 3.部署K8S

1. 添加k8s源

   ```
   cat >/etc/yum.repos.d/kubernetes.repo << EOF
   [kubernetes]
   name=Kubernetes
   baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
   enabled=1
   gpgcheck=0
   repo_gpgcheck=0
   gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
   EOF
   ```

2. 安装 kubeadm，kubelet和kubectl 

   ```
   yum install -y kubelet-1.18.0 kubeadm-1.18.0 kubectl-1.18.0
   ```

4.  初始化初始化kubeadm 

   -  —apiserver-advertise-address 集群公网地址 （阿里云ECS用公网会报错）
   -  —image-repository 由于默认拉取镜像地址k8s.gcr.io国内无法访问，这里指定阿里云镜像仓库地址。
   -  —kubernetes-version K8s版本，与上面安装的一致
   -  —service-cidr 集群内部虚拟网络，Pod统一访问入口
   -  —pod-network-cidr Pod网络，与下面部署的CNI网络组件yaml中保持一致
   -  -proxy-mode  网络代理模式

   ```
   kubeadm init \
   --image-repository registry.aliyuncs.com/google_containers \
   --kubernetes-version v1.18.12 \
   --service-cidr=10.96.0.0/12 \
   --pod-network-cidr=10.244.0.0/16
   ```
   
4. 初始化 kubectl 

   ```
   mkdir ~/.kube
   cp /etc/kubernetes/admin.conf ~/.kube/config
   sudo chown $(id -u):$(id -g) $HOME/.kube/config
   
   //查看集群状态
   kubectl get componentstatus
   ```

5. 修改可暴露端口范围

   ```
   vim /etc/kubernetes/manifests/kube-apiserver.yaml
   //添加
   - --service-node-port-range=1-65535
   
   systemctl daemon-reload
   systemctl restart kubelet
   ```

6. master节点可运行pod(单机的可以使用)

   ```
   kubectl taint nodes --all node-role.kubernetes.io/master-
   ```

   

## 4.网络插件 flannel

2. 添加hosts

   ```
    sudo vi /etc/hosts
    199.232.4.133 raw.githubusercontent.com
   ```
   
3. 安装flannel

   ```
   kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml
   ```
   
2. 开启 ipvs 
4. 创建ingress

   ```
   kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.42.0/deploy/static/provider/cloud/deploy.yaml
   ```

5. 忽略错误

   ```
   kubectl edit ValidatingWebhookConfiguration/ingress-nginx-admission -n ingress-nginx
   validatingwebhookconfiguration.admissionregistration.k8s.io/ingress-nginx-admission edited
   
   failurePolicy: Fail             ##################改成Ignore
   ```

   


   ```
   cat >> /etc/sysctl.conf << EOF
   net.ipv4.ip_forward = 1
   net.bridge.bridge-nf-call-iptables = 1
   net.bridge.bridge-nf-call-ip6tables = 1
   EOF
    
   sysctl -p
   
   yum -y install ipvsadm  ipset
   
   cat > /etc/sysconfig/modules/ipvs.modules <<EOF
   modprobe -- ip_vs
   modprobe -- ip_vs_rr
   modprobe -- ip_vs_wrr
   modprobe -- ip_vs_sh
   modprobe -- nf_conntrack_ipv4
   EOF
   ```

3. 修改kube-proxy

   ```
   kubectl edit cm kube-proxy -n kube-system
   
   修改 mode: "ipvs"  
   ```

4. 重启 kube-proxy 

   ```
   kubectl  get pod -n kube-system | grep kube-proxy | awk '{print $1}' | xargs kubectl delete pod -n kube-system
   ```

   



## 5.部署dashboard可视化插件

1. 安装

   ```
   kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0/aio/deploy/recommended.yaml
   ```

2. 暴露端口（nodePort）

   ```
   kubectl  patch svc kubernetes-dashboard -n kubernetes-dashboard \
   -p '{"spec":{"type":"NodePort","ports":[{"port":443,"targetPort":8443,"nodePort":32567}]}}'
   ```

3. 创建用户

   ```
   kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')
   
   kubectl create clusterrolebinding serviceaccount-cluster-admin   --clusterrole=cluster-admin   --user=system:serviceaccount:kubernetes-dashboard:kubernetes-dashboard
   
   ```

4. 生成Kubeconfig 登录

   在最后加上token： 刚刚生成的token，保存，然后把此文件copy出来。

   ```
   vim /root/.kube/config
   ```

   





