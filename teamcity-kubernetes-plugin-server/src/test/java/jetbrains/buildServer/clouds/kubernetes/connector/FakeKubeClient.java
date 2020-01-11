package jetbrains.buildServer.clouds.kubernetes.connector;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.base.BaseOperation;
import io.fabric8.kubernetes.client.dsl.base.OperationContext;

public class FakeKubeClient extends DefaultKubernetesClient {

  public KubernetesClientException myException;


  @Override
  public MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> pods() {
    if (myException != null){
      throw myException;
    }
    return new MyOperation(){
      @Override
      public Object create(final Object[] resources) throws KubernetesClientException {
        return new Pod();
      }
    };
  }

  private static class MyOperation extends BaseOperation{

    protected MyOperation() {
      super(new OperationContext(null, null, null, null, null, null, null, false, null, null, null, null, null, null,
              null, null, false, 10000, null));
    }
  }
}
