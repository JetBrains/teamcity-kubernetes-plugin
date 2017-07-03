package jetbrains.buildServer.clouds.kubernetes.connector;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 09.06.17.
 */
public enum ImagePullPolicy {
    IfNotPresent {
        @Override
        public String getName() {
            return "IfNotPresent";
        }

        @Override
        public String getDisplayName() {
            return "If Not Present";
        }
    },
    Always {
        @Override
        public String getName() {
            return "Always";
        }

        @Override
        public String getDisplayName() {
            return "Always";
        }
    },
    Never {
        @Override
        public String getName() {
            return "Never";
        }

        @Override
        public String getDisplayName() {
            return "Never";
        }
    };


    public abstract String getName();
    public abstract String getDisplayName();
}
