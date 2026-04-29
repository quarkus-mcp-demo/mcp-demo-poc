## Poc for mcp demo for summit 2026

To run this POC



. git clone https://github.com/quarkus-mcp-demo/mcp-demo-poc
. Make a copy of install/ansible/inventories/inventory.template and save as inventory. Update inventory with the keys needed
. cd install/ansible
. ansible-playbook playbooks/ocp4_workload_mcp_demo_poc.yml -i inventories/inventory
