
package jetbrains.buildServer.clouds.kubernetes.connector;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.base.BaseOperation;
import io.fabric8.kubernetes.client.dsl.base.OperationContext;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class FakeKubeClient extends DefaultKubernetesClient {

  public KubernetesClientException myException;


  @Override
  public MixedOperation<Pod, PodList, PodResource<Pod>> pods() {
    if (myException != null){
      throw myException;
    }
    return new MyOperation(){
      @Override
      protected Object handleCreate(Object resource) {
        return new Pod();
      }
    };
  }

  private static class MyOperation extends BaseOperation{

    protected MyOperation() {
      super(new OperationContext(null, null, null, null, null, null, null, false, null, null, null, null, null, null,
              null, null, false, 10000, null, false, false, null));
    }
  }
}