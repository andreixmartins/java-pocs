import com.hashicorp.cdktf.App;
import com.hashicorp.cdktf.TerraformStack;
import imports.kubernetes.*;
import software.constructs.Construct;

import java.util.Collections;

public class Main extends TerraformStack {
    public Main(final Construct scope, final String id) {
        super(scope, id);
        KubernetesProvider.Builder.create(this, "kind").build();
        Namespace exampleNamespace = new Namespace(this, "tf-cdk-example",
                NamespaceConfig.builder()
                        .metadata(Collections.singletonList(
                                NamespaceMetadata.builder()
                                        .name("tf-cdk-example")
                                        .build()
                        ))
                        .build());

        String app = "nginx-example";
        new Deployment(this, "nginx-deployment", DeploymentConfig.builder()
                .metadata(Collections.singletonList(
                        DeploymentMetadata.builder()
                                .name(app)
                                .namespace(exampleNamespace.getId())
                                .labels(Collections.singletonMap("app", app))
                                .build()
                ))
                .spec(Collections.singletonList(
                        DeploymentSpec.builder()
                                .replicas(2)
                                .selector(Collections.singletonList(
                                        DeploymentSpecSelector.builder()
                                                .matchLabels(Collections.singletonMap("app", app))
                                                .build()
                                ))
                                .template(Collections.singletonList(DeploymentSpecTemplate.builder()
                                        .metadata(Collections.singletonList(
                                                DeploymentSpecTemplateMetadata.builder()
                                                        .labels(Collections.singletonMap("app", app))
                                                        .build()
                                        ))
                                        .spec(Collections.singletonList(
                                                DeploymentSpecTemplateSpec
                                                        .builder()
                                                        .container(Collections.singletonList(
                                                                DeploymentSpecTemplateSpecContainer.builder()
                                                                        .image("nginx:1.7.8")
                                                                        .name("example")
                                                                        .port(Collections.singletonList(
                                                                                DeploymentSpecTemplateSpecContainerPort.builder()
                                                                                        .containerPort(80)
                                                                                        .build()
                                                                        ))
                                                                        .resources(Collections.singletonList(
                                                                                DeploymentSpecTemplateSpecContainerResources.builder()
                                                                                        .limits(Collections.singletonList(
                                                                                                DeploymentSpecTemplateSpecContainerResourcesLimits.builder()
                                                                                                        .cpu("0.5")
                                                                                                        .memory("512Mi")
                                                                                                        .build()
                                                                                        ))
                                                                                        .requests(Collections.singletonList(
                                                                                                DeploymentSpecTemplateSpecContainerResourcesRequests.builder()
                                                                                                        .cpu("250m")
                                                                                                        .memory("50Mi")
                                                                                                        .build()
                                                                                        )).build()
                                                                        )).build()
                                                        )).build()
                                        )).build())
                                ).build()
                )).build());

        new Service(this, "nginx-service", ServiceConfig.builder()
                .metadata(Collections.singletonList(
                        ServiceMetadata.builder()
                                .name(app)
                                .namespace(exampleNamespace.getId())
                                .build()))
                .spec(Collections.singletonList(ServiceSpec.builder()
                        .selector(Collections.singletonMap("app", app))
                        .port(Collections.singletonList(
                                ServiceSpecPort.builder()
                                        .nodePort(30201)
                                        .port(80)
                                        .targetPort("80")
                                        .build()
                        ))
                        .type("NodePort")
                        .build()))
                .build());
    }

    public static void main(String[] args) {
        final App app = new App();
        new Main(app, "tf-cdk-example");
        app.synth();
    }
}